package com.ccloomi.dsengine.bean;

import java.io.Serializable;
import java.util.Map;

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
	public IndexStatus() {}
	public IndexStatus(long position,int offset,Map<String, Long>klMap) {
		this.position=position;
		this.offset=offset;
		this.klMap=klMap;
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
}
