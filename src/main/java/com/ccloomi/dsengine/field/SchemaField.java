package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;

import com.ccloomi.dsengine.analyze.IndexAnalyze;


/**@类名 SchemaField
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月30日-上午9:50:49
 */
public abstract class SchemaField{
	protected String name;
	/**-1表示不定长度*/
	protected int length;
	protected IndexAnalyze analyze;
	/**用于读取*/
	protected byte[]bytes;
	protected int mappedMax;
	/**是否可用于打分*/
	protected boolean scoreable=false;
	protected boolean indexable=true;
	public SchemaField(String name){
		this.name=name;
	}
	public abstract void appendToByteBuffer(ByteBuffer bbuf,Object value);
	public byte[] toBytes(Object value) {
		return null;
	};
	/**将字节数组转换成value*/
	public abstract Object transformValue();
	/**将值转换成字符串*/
	public abstract String transformValue(Object value);
	public String getName() {
		return name;
	}
	public SchemaField setName(String name) {
		this.name = name;
		return this;
	}
	public int getLength() {
		return length;
	}

	public SchemaField setLength(int length) {
		this.length = length;
		if(length>0) {
			this.mappedMax=(Integer.MAX_VALUE/length)*length;
			this.bytes=new byte[length];
		}
		return this;
	}

	public IndexAnalyze getAnalyze() {
		return analyze;
	}

	public SchemaField setAnalyze(IndexAnalyze analyze) {
		this.analyze = analyze;
		return this;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public SchemaField setBytes(byte[] bytes) {
		this.bytes = bytes;
		return this;
	}
	
	public int getMappedMax() {
		return mappedMax;
	}
	public SchemaField setMappedMax(int mappedMax) {
		this.mappedMax = mappedMax;
		return this;
	}
	public boolean isScoreable() {
		return scoreable;
	}
	public SchemaField setScoreable(boolean scoreable) {
		this.scoreable = scoreable;
		if(scoreable&&length>0) {
			this.length+=4;
		}
		return this;
	}
	public boolean isIndexable() {
		return indexable;
	}
	public SchemaField setIndexable(boolean indexable) {
		this.indexable = indexable;
		return this;
	}
}
