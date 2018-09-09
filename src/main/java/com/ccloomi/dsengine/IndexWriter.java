package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;
import static com.ccloomi.dsengine.util.StringUtil.getHashPathFile;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.bean.IndexStatus;
import com.ccloomi.dsengine.field.SchemaField;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IndexWriter
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月25日-下午2:17:12
 */
public class IndexWriter {
	
	private DataAccess da;
	
	private Map<String, long[]>k2lMap;
	/**单位是(*8)KB*/
	private int bufSize=searchReadBufferSize>>13;
	
	//Status
	private Map<String, Long>result;
	private long position=0;
	private int offset=0;
	private int dataLength=0;
	private Schema schema;
	
	public IndexWriter() {
		this.k2lMap=new HashMap<>();
	}
	
	public IndexWriter setSchema(Schema schema) {
		this.schema=schema;
		return this;
	}
	@SuppressWarnings("unchecked")
	public IndexWriter addAndUpdateDocuments(String dataPath,Map<String,? extends Object>...docs) {
		for(int i=0;i<docs.length;i++) {
			addAndUpdateDocument(dataPath, docs[i]);
		}
		return this;
	}
	public IndexWriter addAndUpdateDocument(String dataPath,Map<String, ? extends Object>doc) {
		if(da.hasDeletedDocId()) {
			long docId=da.getDeletedDocId();
			da.updateDocumentByDocId(docId, doc);
		}else {
			addDocument(dataPath, doc);
		}
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
		long docId=((position<<3)+(dataLength<<6)+offset);
		for(Entry<String, ? extends Object>entry:doc.entrySet()) {
			//生成索引数据
			SchemaField sfield=schema.getSchemaField(entry.getKey());
			
			if(sfield.isIndexable()) {
				IndexAnalyze analyze=sfield.getAnalyze();
				String[]ds=analyze.analyze(entry.getValue());

				StringBuilder bkey=new StringBuilder();
				String key=null;
				Long a=null;
				for(int i=0;i<ds.length;i++){
					bkey.delete(0, bkey.length());
					bkey.append(entry.getKey()).append('-').append(ds[i]);
					key=bkey.toString();
					
					a=result.get(key);
					if(a!=null){
						result.put(key, (1l<<offset)|a);
					}else{
						result.put(key, 1l<<offset);
						k2lMap.put(key, new long[bufSize<<10]);
					}
				}
			}
			da.writeFieldData(docId,sfield,entry.getValue());
		}
		offset++;
		if(offset>63){
			for(Entry<String, Long>entry:result.entrySet()){
				long[]bs=k2lMap.get(entry.getKey());
				bs[dataLength]=entry.getValue();
				result.put(entry.getKey(), 0l);
			}
			offset=0;
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
			da.writeFileWithBuffer(path, position, entry.getValue(), dataLength);
		}
		result.clear();
		k2lMap.clear();
		//position:单位是字节
		//dataLength:单位是8个字节
		position+=dataLength<<3;
		dataLength=0;
	}
	public void forceWriteToDisk(String dataPath){
		if(dataLength>0||offset>0){
			for(Entry<String, Long>entry:result.entrySet()){
				long[]bs=k2lMap.get(entry.getKey());
				bs[dataLength]=entry.getValue();
			}
			for(Entry<String, long[]>entry:k2lMap.entrySet()){
				String[]d=entry.getKey().split("-");
				String path=getHashPathFile(Paths.get(dataPath, schema.getName(),d[0]).toString(), d[1], true);
				long[]data=entry.getValue();
				da.writeFileWithBuffer(path, position, data, dataLength+1);
			}
			position+=dataLength<<3;
			da.saveIndexStatus(position, offset, result);
			k2lMap.clear();
			dataLength=0;
		}else{
			writeToDisk(dataPath);
		}
	}
	public void setDataAccess(DataAccess da) {
		this.da = da;
		IndexStatus status=da.getIndexStatus();
		this.position=status.getPosition();
		this.offset=status.getOffset();
		this.result=status.getKlMap();
		//预先创建好已有关键字的long数组
		if(this.offset>0) {
			for(Entry<String, Long>entry:result.entrySet()){
				//*1024 equals <<10
				k2lMap.put(entry.getKey(), new long[bufSize<<10]);
			}
		}
	}
	
}
