package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;

import com.ccloomi.dsengine.analyze.IndexAnalyze;


/**@类名 StringField
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月30日-上午10:14:09
 */
public class StringField extends SchemaField{
	public StringField(String name,int length,IndexAnalyze analyze){
		super(name);
		setLength(length);
		this.analyze=analyze;
		setScoreable(true);
	}
	@Override
	public void appendToByteBuffer(ByteBuffer bbuf, Object value) {
		bbuf.put(toBytes(value));
	}
	@Override
	public byte[] toBytes(Object value) {
		if(value instanceof String) {
			return ((String)value).getBytes();
		}else {
			return String.valueOf(value).getBytes();
		}
	}
	@Override
	public Object transformValue() {
		return bytes2string(bytes);
	}
	@Override
	public String transformValue(Object value) {
		return (String)value;
	}
	private String bytes2string(byte[]bytes){
		int l=0;
		int bl=bytes.length;
		for(int j=0;j<bl;j++){
			if(bytes[j]!=0)l++;
			else break;
		}
		return new String(bytes, 0, l);
	}
}
