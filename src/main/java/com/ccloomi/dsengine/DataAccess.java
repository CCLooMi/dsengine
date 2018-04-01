package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;
import static com.ccloomi.dsengine.util.BytesUtil.revers;
import static com.ccloomi.dsengine.util.StringUtil.baseScore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.rocksdb.RocksDB;

import com.ccloomi.dsengine.bean.IndexStatus;
import com.ccloomi.dsengine.bean.MapBean;
import com.ccloomi.dsengine.field.SchemaField;
import com.ccloomi.dsengine.query.Query;
import com.ccloomi.dsengine.util.BytesUtil;
import com.ccloomi.dsengine.util.StringUtil;
import com.ccloomi.dsengine.util.UUID;

/**© 2015-2018 Chenxj Copyright
 * 类    名：DataAccess
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月31日-上午8:46:35
 */
public class DataAccess {
	private MapBeanPool mbpool;
	
	protected String dpath;
	protected Schema schema;
	protected Map<Long, MappedByteBuffer>fileMBB;
	protected Map<Integer, RandomAccessFile>rafMap;
	/**ReadOnly*/
	protected Map<Integer, RandomAccessFile>rrafMap;
	protected Map<Integer, RocksDB>rdbMap;
	/**ReadOnly*/
	protected Map<Integer, RocksDB>rrdbMap;
	
	public DataAccess(String dpath,Schema schema) {
		this.mbpool=new MapBeanPool();
		
		this.dpath=dpath;
		this.schema=schema;
		this.fileMBB=new HashMap<>();
		this.rafMap=new HashMap<>();
		this.rrafMap=new HashMap<>();
		this.rdbMap=new HashMap<>();
		this.rrdbMap=new HashMap<>();
	}
	public MapBean getMapBean() {
		return mbpool.get();
	}
	public void recycMapBean(MapBean mb) {
		mbpool.recyc(mb);
	}
	public void updateDocument(Object id,Map<String, ? extends Object>doc) {
		byte[]did=null;
		try {
			if(id instanceof byte[]) {
				did=(byte[])id;
			}else {
				did=UUID.fromString((String)id);
			}
			RocksDB rdb=getIdRocksDbReadOnly();
			long docId=BytesUtil.readBytesToLong(rdb.get(did), -1);
			MapBean mb=mbpool.get();
			for(Entry<String, ? extends Object>entry:doc.entrySet()) {
				SchemaField field=schema.getSchemaField(entry.getKey());
				if(field!=null) {
					readFieldData(mb, field, docId);
					updateIndex(docId, field, field
							.getAnalyze()
							.difference(mb.getAttr(entry.getKey()),entry.getValue()));
					//修改field数据
					writeFieldData(docId, field, entry.getValue());
				}
			}
			mbpool.recyc(mb);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 描述：更新索引
	 * 作者：chenxj
	 * 日期：2018年4月1日 - 下午6:13:48
	 * @param docId
	 * @param field
	 * @param change
	 */
	private void updateIndex(long docId,SchemaField field,String[][]change) {
		if(change[0].length>0||change[1].length>0) {
			String filePath=Paths.get(dpath, schema.getName(),field.getName()).toString();
			long position=docId>>3;
			int offset=(int)docId%8;
			String fpath;
			RandomAccessFile raf;
			FileChannel fc;
			ByteBuffer bb=ByteBuffer.allocate(1);
			//处理删除
			String[]del=change[0];
			for(int i=0;i<del.length;i++) {
				fpath=StringUtil.getHashPathFile(filePath,del[i],true);
				raf=getRandomAccessFile(new File(fpath));
				fc=raf.getChannel();
				bb.clear();
				try {
					fc.position(position);
					fc.read(bb);
					bb.flip();
					byte bt=0;
					if(bb.limit()>0) {
						bt=bb.get();
					}
					bt&=~(0b10000000>>>offset);
					bb.clear();
					bb.put(bt);
					bb.flip();
					fc.write(bb, position);
					fc.close();
					raf.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			//处理增加
			String[]add=change[1];
			for(int i=0;i<add.length;i++) {
				fpath=StringUtil.getHashPathFile(filePath,add[i],true);
				raf=getRandomAccessFile(new File(fpath));
				fc=raf.getChannel();
				bb.clear();
				try {
					fc.position(position);
					fc.read(bb);
					bb.flip();
					byte bt=0;
					if(bb.limit()>0) {
						bt=bb.get();
					}
					bt|=(0b10000000>>>offset);
					bb.clear();
					bb.put(bt);
					bb.flip();
					fc.write(bb, position);
					fc.close();
					raf.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void writeFieldData(long docId,SchemaField sfield,Object value) {
		byte[]id=BytesUtil.longToBytes(docId,8,-1);
		//保存Field数据
		try {
			if(sfield.getLength()>0) {
				ByteBuffer bb=ByteBuffer.allocate(sfield.getLength());
				if(sfield.isScoreable()) {
					if(value instanceof String) {
						bb.putFloat(baseScore((String)value));
					}else {
						bb.putFloat(0);
					}
				}
				sfield.appendToByteBuffer(bb, value);
				bb.flip();
				
				RandomAccessFile raf=getDataStorage(sfield);
				raf.getChannel().write(bb, docId*sfield.getLength());
				
				/*如果是ID需要将文档的ID和docId作一个映射
				这样才能通过文档自带的ID来对文档进行操作修改删除等*/
				if("id".equals(sfield.getName())) {
					byte[] did=null;
					if(value!=null) {
						if(value instanceof byte[]) {
							did=(byte[])value;
						}else {
							did=UUID.fromString((String)value);
						}
					}else {
						did=UUID.randomUUID();
					}
					RocksDB rdb=null;
					if(rdbMap.containsKey(sfield.hashCode())) {
						rdb=rdbMap.get(sfield.hashCode());
					}else {
						rdb=getRocksDB(Paths.get(dpath, schema.getName(),sfield.getName()).toFile());
						rdbMap.put(sfield.hashCode(), rdb);
					}
					rdb.put(did, id);
				}
			}else if(sfield.getLength()<0) {
				RocksDB rdb=getDataStorage(sfield);
				if(sfield.isScoreable()) {
					byte[]bytes=sfield.toBytes(value);
					ByteBuffer bb=ByteBuffer.allocate(bytes.length+4);
					if(value instanceof String) {
						bb.putFloat(baseScore((String)value));
					}else {
						bb.putFloat(0);
					}
					bb.put(bytes);
					bb.flip();
					rdb.put(id, bb.array());
				}else {
					rdb.put(id, sfield.toBytes(value));
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void writeFileWithBuffer(String fileName,long position,long[]data,int dataLength){
		RandomAccessFile raf = null;
		int bufSize=1024*256;
		try{
			File file=new File(fileName);
			raf=new RandomAccessFile(file, "rw");
			FileChannel fc=raf.getChannel();
			ByteBuffer buf=ByteBuffer.allocate(bufSize);
			buf.clear();
			int n=bufSize<<3;
			int k=0;
			for(int i=0;i<dataLength;i++,k++){
				buf.putLong(data[i]);
				if(k==n){
					buf.flip();
					fc.write(buf, position);
					buf.compact();
					position+=bufSize;
					k=0;
				}
			}
			if(k>0){
				buf.flip();
				fc.write(buf, position);
				buf.compact();
			}
		}catch (Exception e) {
			if(e instanceof FileNotFoundException){
				System.out.println("文件路径错误，请检查是否有文件名和需要创建的文件夹名称冲突！");
			}
			e.printStackTrace();
		}finally {
			try {raf.close();}
			catch (IOException e) {}
		}
	}
	/**
	 * 描述：读取查询中Field数据到MapBean
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:50:44
	 * @param query 查询对象
	 * @param docId 文档ID
	 * @return
	 */
	public MapBean readDocumentFieldDataInQuery(Query query,Long docId) {
		MapBean mb=mbpool.get();
		for(String fieldName:query.getMtree().keySet()) {
			SchemaField field=schema.getSchemaField(fieldName);
			if(field!=null) {
				readFieldData(mb, field, docId);
			}
		}
		mb.setDocId(docId);
		return mb;
	}
	/**
	 * 描述：读取剩余Field数据
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:51:41
	 * @param docs 文档列表
	 * @return
	 */
	public MapBean[] repairDoc(MapBean[]docs){
		for(int i=0;i<docs.length;i++){
			for(SchemaField field:schema.getFieldsArray()) {
				if(!docs[i].hasAttr(field.getName())) {
					readFieldData(docs[i], field, docs[i].getDocId());
				}
			}
		}
		return docs;
	}
	/**
	 * @名称 saveStatus
	 * @说明	保存状态
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月29日-下午5:25:06
	 * @param status
	 */
	public void saveIndexStatus(IndexStatus status) {
		ObjectOutputStream objOut=null;
		try {
			FileOutputStream fOut = new FileOutputStream(Paths
					.get(dpath, schema.getName(),".status")
					.toFile());
			objOut=new ObjectOutputStream(fOut);
			objOut.writeObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {objOut.close();} catch (IOException e) {};
		}
	}
	/**
	 * @名称 loadIndexStatus
	 * @说明	恢复状态
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月29日-下午5:34:02
	 */
	public IndexStatus loadIndexStatus() {
		File file=Paths.get(dpath, schema.getName(),".status").toFile();
		if(file.exists()){
			ObjectInputStream objIn=null;
			try{
				objIn=new ObjectInputStream(new FileInputStream(file));
				return (IndexStatus) objIn.readObject();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try{objIn.close();}
				catch(Exception e){};
			}
		}
		return new IndexStatus();
	}
	/**
	 * 描述：读取Field数据
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:52:50
	 * @param mbean 存储到MapBean中
	 * @param field 读取的Field对象
	 * @param docId 文档ID
	 */
	private void readFieldData(MapBean mbean,SchemaField field,long docId) {
		try {
			if(field.getLength()>0) {
				MappedByteBuffer mbb=null;
				long fs=docId*field.getLength();
				int p=(int)(fs/field.getMappedMax());
				int ps=(int)(fs%field.getMappedMax());
				//使用数字作为key来提高性能
				Long key=new Long(field.hashCode()|p);
				if(fileMBB.containsKey(key)) {
					mbb=fileMBB.get(key);
				}else {
					RandomAccessFile raf=getDataReader(field);
					long fsize=raf.length();
					long pmax=p*field.getMappedMax();
					if(pmax+field.getMappedMax()>fsize) {
						mbb=raf.getChannel().map(FileChannel.MapMode.READ_ONLY, pmax, fsize-pmax);
					}else {
						mbb=raf.getChannel().map(FileChannel.MapMode.READ_ONLY, pmax, field.getMappedMax());
					}
				}
				mbb.position(ps);
				if(field.isScoreable()) {
					mbean.setAttrScore(field.getName(), mbb.getFloat());
				}
				mbb.get(field.getBytes());
				mbean.setAttr(field.getName(), field.transformValue());
			}else if(field.getLength()<0) {
				RocksDB rdb=getDataReader(field);
				byte[]bytes=rdb.get(BytesUtil.longToBytes(docId, 8, -1));
				if(field.isScoreable()) {
					ByteBuffer mbb=MappedByteBuffer.allocate(bytes.length);
					mbb.put(bytes);
					mbb.flip();
					mbean.setAttrScore(field.getName(), mbb.getFloat());
					bytes=new byte[mbb.remaining()];
					mbb.get(bytes);
					field.setBytes(bytes);
					mbean.setAttr(field.getName(), field.transformValue());
				}else {
					field.setBytes(bytes);
					mbean.setAttr(field.getName(), field.transformValue());
				}
			}else {
				if(field.isScoreable()) {
					mbean.setAttr(field.getName(), "");
					mbean.setAttrScore(field.getName(), 0);
				}else {
					mbean.setAttr(field.getName(), "");
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			if(field.isScoreable()) {
				mbean.setAttr(field.getName(), "");
				mbean.setAttrScore(field.getName(), 0);
			}else {
				mbean.setAttr(field.getName(), "");
			}
		}
	}
	/**
	 * 描述：读取索引文件数据
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:54:53
	 * @param filePath 索引文件路径
	 * @param fname 关键字的名称
	 * @param position 开始读取位置
	 * @param bytes 读取结果
	 * @return
	 */
	public short readIndexData(String filePath,String fname,long position,byte[]bytes){
		if(fname.charAt(0)=='-'){
			MappedByteBuffer mbb=getIndexMBB(filePath,fname.substring(1, fname.length()),position);
			if(mbb!=null){
				int l=searchReadBufferSize;
				if(position+searchReadBufferSize>mbb.limit()){
					l=(int)(mbb.limit()-position);
				}
				mbb.position((int) (position%Integer.MAX_VALUE));
				mbb.get(bytes,0,l);
				return revers(bytes);
			}else {
				return -2;
			}
		}else{
			MappedByteBuffer mbb=getIndexMBB(filePath, fname, position);
			if(mbb!=null){
				int l=searchReadBufferSize;
				if(position+searchReadBufferSize>mbb.limit()){
					l=(int)(mbb.limit()-position);
				}
				mbb.position((int) (position%Integer.MAX_VALUE));
				mbb.get(bytes,0,l);
			}else {
				return -2;
			}
		}
		return -1;
	}
	/**
	 * 描述：获取索引文件的MappedByteBuffer
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:34:44
	 * @param filePath index file path
	 * @param fname keyword name
	 * @param position
	 * @return
	 */
	private MappedByteBuffer getIndexMBB(String filePath,String fname,long position){
		RandomAccessFile raf = null;
		MappedByteBuffer mbb = null;
		String fpath=StringUtil.getHashPathFile(filePath,fname,false);
		int p=(int) (position/Integer.MAX_VALUE);
		long key=((long)fpath.hashCode()<<32)|p;
		try{
			if(fileMBB.containsKey(key)){
				mbb=fileMBB.get(key);
			}else{
				raf=new RandomAccessFile(fpath, "rw");
				FileChannel fc=raf.getChannel();
				long fsize=fc.size();
				long pmax=p*Integer.MAX_VALUE;
				if(pmax+Integer.MAX_VALUE>fsize){
					mbb=fc.map(FileChannel.MapMode.READ_WRITE, pmax, fsize-pmax);
				}else{
					mbb=fc.map(FileChannel.MapMode.READ_WRITE, pmax, Integer.MAX_VALUE);
				}
				fileMBB.put(key, mbb);
			}
		}catch (Exception e) {fileMBB.put(key, null);}
		return mbb;
	}
	private RocksDB getIdRocksDbReadOnly() {
		SchemaField field=schema.getSchemaField("id");
		if(rrdbMap.containsKey(field.hashCode())) {
			return rrdbMap.get(field.hashCode());
		}else {
			RocksDB rdb=getRocksDBReadOnly(Paths.get(dpath, schema.getName(),field.getName()).toFile());
			rrdbMap.put(field.hashCode(), rdb);
			return rdb;
		}
	}
	private RocksDB getIdRocksDb() {
		SchemaField field=schema.getSchemaField("id");
		if(rdbMap.containsKey(field.hashCode())) {
			return rdbMap.get(field.hashCode());
		}else {
			RocksDB rdb=getRocksDB(Paths.get(dpath, schema.getName(),field.getName()).toFile());
			rdbMap.put(field.hashCode(), rdb);
			return rdb;
		}
	}
	/**
	 * 描述：获取Field文件读取对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:56:22
	 * @param dataPath 存储地址
	 * @param field Field对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T>T getDataReader(SchemaField field){
		if(field.getLength()>0) {
			if(rrafMap.containsKey(field.hashCode())) {
				return (T)rrafMap.get(field.hashCode());
			}else {
				File dir=Paths.get(dpath, schema.getName(),field.getName()).toFile();
				if(!dir.exists()) {
					dir.mkdirs();
				}
				RandomAccessFile raf=getRandomAccessFileReadOnly(Paths.get(dir.getAbsolutePath(),field.getName()).toFile());
				rrafMap.put(field.hashCode(), raf);
				return (T)raf;
			}
		}else if(field.getLength()<0) {
			if(rrdbMap.containsKey(field.hashCode())) {
				return (T)rrdbMap.get(field.hashCode());
			}else {
				RocksDB rdb=getRocksDBReadOnly(Paths.get(dpath, schema.getName(),field.getName()).toFile());
				rrdbMap.put(field.hashCode(), rdb);
				return (T)rdb;
			}
		}
		return null;
	}
	/**
	 * 描述：获取文件读写对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午11:07:39
	 * @param dataPath 存储地址
	 * @param field Field对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T>T getDataStorage(SchemaField field){
		if(field.getLength()>0) {
			if(rafMap.containsKey(field.hashCode())) {
				return (T)rafMap.get(field.hashCode());
			}else {
				File dir=Paths.get(dpath, schema.getName(),field.getName()).toFile();
				if(!dir.exists()) {
					dir.mkdirs();
				}
				RandomAccessFile raf=getRandomAccessFile(Paths.get(dir.getAbsolutePath(),field.getName()).toFile());
				rafMap.put(field.hashCode(), raf);
				return (T)raf;
			}
		}else if(field.getLength()<0) {
			if(rdbMap.containsKey(field.hashCode())) {
				return (T)rdbMap.get(field.hashCode());
			}else {
				RocksDB rdb=getRocksDB(Paths.get(dpath, schema.getName(),field.getName()).toFile());
				rdbMap.put(field.hashCode(), rdb);
				return (T)rdb;
			}
		}
		return null;
	}
	/**
	 * 描述：获取只读RocksDB对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:57:28
	 * @param file 
	 * @return
	 */
	private RocksDB getRocksDBReadOnly(File file) {
		try {
			RocksDB rdb=RocksDB.openReadOnly(file.getAbsolutePath());
			return rdb;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 描述：获取Rocks读写对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午11:05:39
	 * @param file
	 * @return
	 */
	private RocksDB getRocksDB(File file) {
		try {
			RocksDB rdb=RocksDB.open(file.getAbsolutePath());
			return rdb;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 描述：获取RandomAccessFile读写对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午11:06:02
	 * @param file
	 * @return
	 */
	private RandomAccessFile getRandomAccessFile(File file) {
		try {
			return new RandomAccessFile(file, "rw");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 描述：获取只读RandomAccessFile对象
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:58:32
	 * @param file
	 * @return
	 */
	private RandomAccessFile getRandomAccessFileReadOnly(File file) {
		try {
			return new RandomAccessFile(file, "r");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
