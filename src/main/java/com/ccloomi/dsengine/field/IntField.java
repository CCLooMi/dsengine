package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;

import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.util.BytesUtil;


/**@类名 IntField
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月30日-上午10:20:09
 */
public class IntField extends SchemaField{
	public IntField(String name,IndexAnalyze analyze){
		super(name);
		setLength(4);
		this.analyze=analyze;
	}
	@Override
	public void appendToByteBuffer(ByteBuffer bbuf, Object value) {
		if(value instanceof Integer){
			bbuf.putInt((int)value);
		}else {
			bbuf.putInt(Integer.valueOf((String)value));
		}
	}
	@Override
	public Object transformValue() {
		return BytesUtil.readBytesToInt(bytes, -1);
	}
	@Override
	public String transformValue(Object value) {
		if(value instanceof String){
			return (String)value;
		}else{
			return String.valueOf(value);
		}
	}
}
