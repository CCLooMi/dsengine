package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;

import java.nio.file.Paths;
import java.util.Map;

import com.ccloomi.dsengine.bean.IndexStatus;
import com.ccloomi.dsengine.bean.ResultBean;
import com.ccloomi.dsengine.linkthread.SchemaReader;
import com.ccloomi.dsengine.query.Query;
import com.ccloomi.dsengine.query.QueryParser;

/**© 2015-2018 Chenxj Copyright
 * 类    名：DSEngine
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午5:31:56
 */
public class DSEngine {
	private int[]m0;
	private String dPath;
	private IndexReader indexReader;
	private IndexWriter indexWriter;
	private SchemaReader schemaReader;
	private DataAccess da;
	private IndexStatus indexStatus;
	private String schemaPath;
	private Schema schema;
	
	public DSEngine(String dp) {
		m0=new int[129];
		dPath=dp;
		indexReader=new IndexReader();
		indexWriter=new IndexWriter();
		for(int i=0;i<8;i++) {
			m0[1<<i]=i;
		}
	}
	public DSEngine updateDocument(Map<String, ? extends Object>doc) {
		Object id=doc.remove("id");
		if(id instanceof String) {
			da.updateDocumentById((String)id, doc);
		}else if(id instanceof byte[]) {
			da.updateDocumentById((byte[])id, doc);
		}
		return this;
	}
	public DSEngine updateDocumentByDocId(Object docId,Map<String, ? extends Object>doc) {
		if(docId instanceof Long) {
			da.updateDocumentByDocId((long)docId, doc);
		}else if(docId instanceof byte[]) {
			da.updateDocumentByDocId((byte[])docId, doc);
		}
		return this;
	}
	public ResultBean search(Map<String, Object>qm) {
		return doQuery(QueryParser.parser(schema, qm));
	}
	public ResultBean search(String qStr) {
		return doQuery(QueryParser.parser(schema, qStr));
	}
	private ResultBean doQuery(Query query) {
		this.schemaReader.setQuery(query);
		ResultBean resultBean=new ResultBean();
		byte[]result=new byte[searchReadBufferSize];;
		long countDocs=0;
		long t1=System.currentTimeMillis();
		long indexFileLength=indexStatus.indexFileLength();
		long totalDocs=indexStatus.totalDocs();
		query:for(long i=0;i<indexFileLength;i+=searchReadBufferSize){
			indexReader.calculate(schemaPath, query, i, result);
			//根据result获取文档ID并异步进行查找
			int rl=result.length;
			int count=0;
			for(int j=0;j<rl;j++){
				int b=result[j]&0xff;
				while(b>0) {
					long docid=new Long(((i+j)<<3)+m0[b&-b]);
					if(docid<totalDocs) {
						schemaReader.addData(docid);
						count++;
					}else {
						countDocs+=count;
						break query;
					}
					b-=(b&-b);
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
		resultBean.setHits(schemaReader.get());
		
		return resultBean;
	}
	@SuppressWarnings("unchecked")
	public DSEngine addDocuments(Map<String,? extends Object>...docs) {
		indexWriter.addDocuments(dPath, docs);
		return this;
	}
	@SuppressWarnings("unchecked")
	public DSEngine addAndUpdateDocuments(Map<String, ? extends Object>...docs) {
		indexWriter.addAndUpdateDocuments(dPath, docs);
		return this;
	}
	public DSEngine deleteDocumentById(String id) {
		da.deleteDocumentById(id);
		return this;
	}
	public DSEngine deleteDocumentById(byte[] id) {
		da.deleteDocumentById(id);
		return this;
	}
	public DSEngine deleteDocumentByDocId(long docId) {
		da.deleteDocumentByDocId(docId);
		return this;
	}
	public DSEngine deleteDocumentByDocId(byte[] docId) {
		da.deleteDocumentByDocId(docId);
		return this;
	}
	public DSEngine setSchema(Schema schema) {
		this.schema=schema;
		this.schemaPath=Paths.get(dPath, schema.getName()).toString();
		this.da=new DataAccess(dPath,schema);
		this.indexWriter.setSchema(schema);
		this.indexWriter.setDataAccess(da);
		this.schemaReader=new SchemaReader(da);
		this.indexReader.setDataAccess(da);
		this.indexStatus=da.getIndexStatus();
		return this;
	}
	/**
	 * 描述：添加了文档之后一定要先存储到磁盘，不然后面的删除修改等操作会导致索引错误
	 * 作者：chenxj
	 * 日期：2018年4月5日 - 下午8:25:03
	 * @return
	 */
	public DSEngine flush2disk() {
		indexWriter.forceWriteToDisk(dPath);
		return this;
	}
}
