package com.ccloomi.dsengine.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IndexStatus
 * 类 描 述：Shcema索引状态Bean
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月31日-下午2:38:16
 */
public class IndexStatus implements Serializable{
	private static final long serialVersionUID = 7567690743394157129L;
	private long position;
	private int offset;
	private Map<String, Long>klMap;
	private Queue<Long>deletedDocIds;
	public IndexStatus() {
		this.position=0;
		this.offset=63;
		this.klMap=new HashMap<>();
		this.deletedDocIds=new ConcurrentLinkedQueue<>();
	}
	public boolean hashDeletedDocId() {
		return this.deletedDocIds.isEmpty();
	}
	public long getDeletedDocId() {
		return this.deletedDocIds.poll();
	}
	//需要保证position是最新的
	public IndexStatus addDeletedDocIds(long...docIds) {
		for(int i=0;i<docIds.length;i++) {
			this.deletedDocIds.add(docIds[i]);
			//如果docid在result中
			if(docIds[i]>=(position<<3)) {
				int ofset=63-(int)docIds[i]%64;
				for(Entry<String, Long>entry:klMap.entrySet()) {
					long v=entry.getValue();
					v&=~(1l<<ofset);
					entry.setValue(v);
				}
			}
		}
		return this;
	}
	//需要保证position是最新的
	public IndexStatus updateIndex(String[][]change,long...docIds) {
		for(int ii=0;ii<docIds.length;ii++) {
			if(docIds[ii]>=(position<<3)) {
				int ofset=63-(int)docIds[ii]%64;
				if(change[0].length>0||change[1].length>0) {
					//处理删除
					String[]del=change[0];
					for(int i=0;i<del.length;i++) {
						long v=klMap.get(del[i]);
						v&=~(1l<<ofset);
						klMap.put(del[i], v);
					}
					//处理增加
					String[]add=change[1];
					for(int i=0;i<add.length;i++) {
						long v=klMap.get(add[i]);
						v|=(1l<<ofset);
						klMap.put(add[i], v);
					}
				}
			}
		}
		return this;
	}
	public long totalDocs() {
		return (this.position<<3)+(63-offset);
	}
	public long indexFileLength() {
		if(this.offset>0) {
			return this.position+8;
		}else {
			return this.position;
		}
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public Map<String, Long> getKlMap() {
		return klMap;
	}
	public void setKlMap(Map<String, Long> klMap) {
		this.klMap = klMap;
	}
	public Queue<Long> getDeletedDocIds() {
		return deletedDocIds;
	}
	public void setDeletedDocIds(Queue<Long> deletedDocIds) {
		this.deletedDocIds = deletedDocIds;
	}
}
