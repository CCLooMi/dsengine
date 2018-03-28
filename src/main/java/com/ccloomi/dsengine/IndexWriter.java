package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;
import static com.ccloomi.dsengine.util.StringUtil.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.rocksdb.RocksDB;

import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.field.SchemaField;
import com.ccloomi.dsengine.util.BytesUtil;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IndexWriter
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月25日-下午2:17:12
 */
public class IndexWriter {
	private Map<String, long[]>k2lMap;
	/**CSV数据导入需要使用*/
	private SchemaField[] sfArray;
	/**单位是(*8)KB*/
	private int bufSize=searchReadBufferSize>>13;
	
	//Status
	private Map<String, Long>result;
	private long position=0;
	private int offset=63;
	private int dataLength=0;
	private Schema schema;
	
	public IndexWriter() {
		this.k2lMap=new HashMap<>();
		this.result=new HashMap<>();
	}
	
	public IndexWriter setSchema(Schema schema) {
		this.schema=schema;
		return this;
	}
	@SuppressWarnings("unchecked")
	public IndexWriter addDocuments(String dataPath,Map<String,? extends Object>...docs) {
		for(int i=0;i<docs.length;i++) {
			addDocument(dataPath, docs[i]);
		}
		return this;
	}
	public IndexWriter addDocument(String dataPath,Map<String,? extends Object>doc) {
		for(Entry<String, ? extends Object>entry:doc.entrySet()) {
			//生成索引数据
			SchemaField sfield=schema.getSchemaField(entry.getKey());
			
			if(sfield.isIndexable()) {
				IndexAnalyze analyze=sfield.getAnalyze();
				String[]ds=analyze.analyze(entry.getValue());
				for(String dt:ds){
					String key=entry.getKey()+"-"+dt;
					Long a=result.get(key);
					if(a!=null){
						result.put(key, (1l<<offset)|a);
					}else{
						result.put(key, 1l<<offset);
						k2lMap.put(key, new long[1024*bufSize]);
					}
				}
			}
			//TODO  需要保存Field数据
			try {
				long docId=((position<<3)+(dataLength<<6)+(63-offset));
				if(sfield.getLength()>0) {
					ByteBuffer bb=ByteBuffer.allocate(sfield.getLength());
					//TODO
					if(sfield.isScoreable()) {
						if(entry.getValue() instanceof String) {
							bb.putFloat(baseScore((String)entry.getValue()));
						}else {
							bb.putFloat(0);
						}
					}
					sfield.appendToByteBuffer(bb, entry.getValue());
					bb.flip();
					
					RandomAccessFile raf=getDataStorage(dataPath, sfield);
					raf.getChannel().write(bb, docId*sfield.getLength());
				}else if(sfield.getLength()<0) {
					RocksDB rdb=getDataStorage(dataPath, sfield);
					byte[]id=BytesUtil.longToBytes(docId,8,-1);
					//TODO
					if(sfield.isScoreable()) {
						byte[]bytes=sfield.toBytes(entry.getValue());
						ByteBuffer bb=ByteBuffer.allocate(bytes.length+4);
						if(entry.getValue() instanceof String) {
							bb.putFloat(baseScore((String)entry.getValue()));
						}else {
							bb.putFloat(0);
						}
						bb.put(bytes);
						bb.flip();
						rdb.put(id, bb.array());
					}else {
						rdb.put(id, sfield.toBytes(entry.getValue()));
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		offset--;
		if(offset<0){
			for(Entry<String, Long>entry:result.entrySet()){
				long[]bs=k2lMap.get(entry.getKey());
				bs[dataLength]=entry.getValue();
				result.put(entry.getKey(), 0l);
			}
			offset=63;
			dataLength++;
			if(dataLength>>10>=bufSize){
				writeToDisk(dataPath);
			}
		}
		return this;
	}
	private void writeToDisk(String dataPath){
		for(Entry<String, long[]>entry:k2lMap.entrySet()){
			String[]d=entry.getKey().split("-");
			String path=getHashPathFile(Paths.get(dataPath, schema.getName(),d[0]).toString(), d[1], true);
			writeFileWithBuffer(path, position, entry.getValue(), dataLength);
		}
		result.clear();
		k2lMap.clear();
		//position:单位是字节
		//dataLength:单位是8个字节
		position+=dataLength<<3;
		dataLength=0;
	}
	public void forceWriteToDisk(String dataPath){
		if(dataLength>0||offset<63){
			for(Entry<String, Long>entry:result.entrySet()){
				long[]bs=k2lMap.get(entry.getKey());
				bs[dataLength]=entry.getValue();
				result.put(entry.getKey(), 0l);
			}
			for(Entry<String, long[]>entry:k2lMap.entrySet()){
				String[]d=entry.getKey().split("-");
				String path=getHashPathFile(Paths.get(dataPath, schema.getName(),d[0]).toString(), d[1], true);
				long[]data=entry.getValue();
				writeFileWithBuffer(path, position, data, dataLength+1);
			}
			position+=dataLength<<3;
			saveStatus(dataPath);
			result.clear();
			k2lMap.clear();
			dataLength=0;
		}else{
			writeToDisk(dataPath);
		}
	}
	
	private Map<Integer, RandomAccessFile>rafMap=new HashMap<>();
	private Map<Integer, RocksDB>rdbMap=new HashMap<>();
	
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
				RandomAccessFile raf=getRandomAccessFile(Paths.get(dataPath, schema.getName(),field.getName()).toFile(), "rw");
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
			RocksDB rdb=RocksDB.open(file.getAbsolutePath());
			return rdb;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private RandomAccessFile getRandomAccessFile(File file,String mode) {
		try {
			return new RandomAccessFile(file, mode);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @名称 saveStatus
	 * @说明	保存状态
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月29日-下午5:25:06
	 */
	private void saveStatus(String dataPath){
		ObjectOutputStream objOut=null;
		BufferedOutputStream bout=null;
		try {
			FileOutputStream psOut = new FileOutputStream(Paths
					.get(dataPath, schema.getName(),".psof")
					.toFile());
			FileOutputStream kbOut=new FileOutputStream(Paths
					.get(dataPath, schema.getName(),".kb")
					.toFile());
			objOut=new ObjectOutputStream(kbOut);
			objOut.writeObject(result);
			bout=new BufferedOutputStream(psOut);
			bout.write((position+"."+offset).getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {objOut.close();} catch (IOException e) {};
			try {bout.close();} catch (IOException e) {};
		}
	}
	/**
	 * @名称 restoreStatus
	 * @说明	恢复状态
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月29日-下午5:34:02
	 */
	@SuppressWarnings("unchecked")
	private void restoreStatus(String dataPath){
		File ps=Paths.get(dataPath, schema.getName(),".psof").toFile();
		if(ps.exists()){
			BufferedReader reader=null;
			ObjectInputStream objIn=null;
			try{
				reader=new BufferedReader(new FileReader(ps));
				String[]data=reader.readLine().split("\\.");
				position=Long.valueOf(data[0]);
				offset=Integer.valueOf(data[1]);
				if(offset<63){
					objIn=new ObjectInputStream(new FileInputStream(Paths
							.get(dataPath, schema.getName(),".kb")
							.toFile()));
					result=(Map<String, Long>) objIn.readObject();
					for(Entry<String, Long>entry:result.entrySet()){
						k2lMap.put(entry.getKey(), new long[1024*bufSize]);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try{reader.close();objIn.close();}
				catch(Exception e){};
			}
		}
	}
	private void writeFileWithBuffer(String fileName,long position,long[]data,int dataLength){
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
}
