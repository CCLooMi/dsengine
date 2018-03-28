package com.ccloomi.dsengine.bean;

import java.util.HashMap;
import java.util.Map;

/**@类名 MapBean
 * @说明	用来代替Map存储文档数据,性能差不多是map的4倍还多
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-上午11:46:52
 */
public class MapBean {
	private long docId;
	private int ps;
	private float score;
	private Map<String, Object>attrMap;
	private Map<String, Float>scoreMap;
	public MapBean() {
		this.attrMap=new HashMap<>();
		this.scoreMap=new HashMap<>();
	}
	public boolean hasAttr(String attrName) {
		return attrMap.containsKey(attrName);
	}
	public void setAttr(String attrName,Object attrValue) {
		this.attrMap.put(attrName, attrValue);
	}
	public Object getAttr(Object attrName) {
		return this.attrMap.get(attrName);
	}
	
	public void setAttrScore(String attrName,float attrScore) {
		this.scoreMap.put(attrName, attrScore);
	}
	public float getAttrScore(Object attrName) {
		return this.scoreMap.get(attrName);
	}
	
	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
	public int getPosition() {
		return ps;
	}

	public void setPs(int ps) {
		this.ps = ps;
	}

	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	
	public Map<String, Object> getAttrMap() {
		return attrMap;
	}

	public Map<String, Float> getScoreMap() {
		return scoreMap;
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("{docId:").append(docId)
		.append(",score:"+score)
		.append(",attrMap:").append(attrMap)
		.append(",scoreMap:").append(scoreMap)
		.append('}');
		return sb.toString();
	}
}
