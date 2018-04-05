package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchMaxRate;
import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;

import java.util.Map;

import com.ccloomi.dsengine.bean.IndexStatus;
import com.ccloomi.dsengine.bean.ResultBean;
import com.ccloomi.dsengine.linkthread.SchemaReader;
import com.ccloomi.dsengine.query.Query;

/**© 2015-2018 Chenxj Copyright
 * 类    名：DSEngine
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午5:31:56
 */
public class DSEngine {
	private String dPath;
	private StringBuilder dataPath;
	private IndexReader indexReader;
	private IndexWriter indexWriter;
	private SchemaReader schemaReader;
	private DataAccess da;
	
	private long indexFileLength;
	private long totalDocs;
	
	public DSEngine(String dp) {
		dPath=dp;
		dataPath=new StringBuilder(dp);
		indexReader=new IndexReader();
		indexWriter=new IndexWriter();
	}
	public void updateDocument(Map<String, ? extends Object>doc) {
		Object id=doc.remove("id");
		if(id instanceof String) {
			da.updateDocumentById((String)id, doc);
		}else if(id instanceof byte[]) {
			da.updateDocumentById((byte[])id, doc);
		}
	}
	public ResultBean doQuery(String schemaName,Query query) {
		this.schemaReader.setQuery(query);
		ResultBean resultBean=new ResultBean();
		byte[]result=new byte[searchReadBufferSize];;
		long countDocs=0;
		long t1=System.currentTimeMillis();
		query:for(long i=0;i<indexFileLength;i+=searchReadBufferSize){
			indexReader.calculate(getSchemaPath(schemaName), query, i, result);
			//根据result获取文档ID并异步进行查找
			int rl=result.length;
			int count=0;
			for(int j=0;j<rl;j++){
				byte b=result[j];
				if(b!=0){
					for(byte offset=7;offset>-1;offset--){
						if((b&(1<<offset))!=0){
							long docid=new Long(((i+j)<<3)+7-offset);
							if(docid<totalDocs) {
								count++;
								schemaReader.addData(docid);
								//大部分正常的查询是不会超过这个比例的，恶意查询会被立马阻止
								if((float)count/(searchReadBufferSize<<3)>searchMaxRate){
									countDocs+=count;
									break query;
								}
							}else {
								countDocs+=count;
								break query;
							}
						}
					}
				}
			}
			countDocs+=count;
		}
		resultBean.setTook((int) (System.currentTimeMillis()-t1));
		resultBean.setTotal(countDocs);
		
		if(countDocs>0){
			schemaReader.setTotal(countDocs);
		}else{
			schemaReader.setTotal(-1);
		}
		schemaReader.complete();
		//必须在调用complete方法之后才能调用get，不然无法退出
		resultBean.setHits(schemaReader.get());
		
		return resultBean;
	}
	public DSEngine setSchema(Schema schema) {
		this.da=new DataAccess(dPath,schema);
		this.indexWriter.setSchema(schema);
		this.indexWriter.setDataAccess(da);
		this.schemaReader=new SchemaReader(da);
		this.indexReader.setDataAccess(da);
		schemaPosition();
		return this;
	}
	@SuppressWarnings("unchecked")
	public DSEngine addDocuments(Map<String,? extends Object>...docs) {
		indexWriter.addDocuments(dPath, docs);
		return this;
	}
	public DSEngine flush2disk() {
		indexWriter.forceWriteToDisk(dPath);
		return this;
	}
	private void schemaPosition() {
		IndexStatus status=da.getIndexStatus();
		this.indexFileLength=status.indexFileLength();
		this.totalDocs=status.totalDocs();
	}
	private String getSchemaPath(String schemaName) {
		dataPath.delete(dPath.length(), dataPath.length());
		dataPath.append('/').append(schemaName);
		return dataPath.toString();
	}
}
