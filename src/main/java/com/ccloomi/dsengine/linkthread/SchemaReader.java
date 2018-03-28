package com.ccloomi.dsengine.linkthread;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.rocksdb.RocksDB;

import com.ccloomi.dsengine.Schema;
import com.ccloomi.dsengine.bean.MapBean;
import com.ccloomi.dsengine.field.SchemaField;
import com.ccloomi.dsengine.util.BytesUtil;


/**@类名 SchemaReader
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-下午1:47:16
 */
public class SchemaReader extends BaseLinkedThread<Long, MapBean> implements Closeable{
	private Map<Long, MappedByteBuffer>fileMBB;
	private Map<Integer, RandomAccessFile>rafMap;
	private Map<Integer, RocksDB>rdbMap;
	private String dPath;
	public SchemaReader(Schema schema) {
		this.setName("LinkThread_A-Schema Reader");
		this.schema=schema;
		this.fileMBB=new HashMap<>();
		this.rafMap=new HashMap<>();
		this.rdbMap=new HashMap<>();
		this.setNextThread(new Score(schema))
		.setNextThread(new Sort(schema));
	}

	@Override
	public void close() throws IOException {
		// TODO 需要关闭所有打开的文件资源
	}

	@Override
	public void reset() {}

	@Override
	public MapBean processData(Long docId) {
		MapBean mb=schema.getMbpool().get();
		for(String fieldName:query.getMtree().keySet()) {
			SchemaField field=schema.getSchemaField(fieldName);
			if(field!=null) {
				getFieldData(mb, field, docId);
			}
		}
		mb.setDocId(docId);
		return mb;
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get() {
		if(nextThread!=null) {
			return (T) repairDoc(nextThread.get());
		}
		return null;
	}
	private MapBean[] repairDoc(MapBean[]docs){
		for(int i=0;i<docs.length;i++){
			for(SchemaField field:schema.getFieldsArray()) {
				if(!docs[i].hasAttr(field.getName())) {
					getFieldData(docs[i], field, docs[i].getDocId());
				}
			}
		}
		return docs;
	}
	private void getFieldData(MapBean mbean,SchemaField field,long position) {
		try {
			if(field.getLength()>0) {
				MappedByteBuffer mbb=null;
				int p=(int)(position/field.getMappedMax());
				int ps=(int)((position*field.getLength())%field.getMappedMax());
				//使用数字作为key来提高性能
				Long key=new Long(field.hashCode()|p);
				if(fileMBB.containsKey(key)) {
					mbb=fileMBB.get(key);
				}else {
					RandomAccessFile raf=getDataStorage(dPath, field);
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
				RocksDB rdb=getDataStorage(dPath, field);
				byte[]bytes=rdb.get(BytesUtil.longToBytes(position, 8, -1));
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
	
	@SuppressWarnings("unchecked")
	private <T>T getDataStorage(String dataPath,SchemaField field){
		if(field.getLength()>0) {
			if(rafMap.containsKey(field.hashCode())) {
				return (T)rafMap.get(field.hashCode());
			}else {
				File dir=Paths.get(dataPath, schema.getName()).toFile();
				if(!dir.exists()) {
					dir.mkdirs();
				}
				RandomAccessFile raf=getRandomAccessFile(Paths.get(dataPath, schema.getName(),field.getName()).toFile());
				rafMap.put(field.hashCode(), raf);
				return (T)raf;
			}
		}else if(field.getLength()<0) {
			if(rdbMap.containsKey(field.hashCode())) {
				return (T)rdbMap.get(field.hashCode());
			}else {
				RocksDB rdb=getRocksDB(Paths.get(dataPath, schema.getName(),field.getName()).toFile());
				rdbMap.put(field.hashCode(), rdb);
				return (T)rdb;
			}
		}
		return null;
	}
	private RocksDB getRocksDB(File file) {
		try {
			RocksDB rdb=RocksDB.openReadOnly(file.getAbsolutePath());
			return rdb;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private RandomAccessFile getRandomAccessFile(File file) {
		try {
			return new RandomAccessFile(file, "r");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getdPath() {
		return dPath;
	}

	public void setdPath(String dPath) {
		this.dPath = dPath;
	}
}
