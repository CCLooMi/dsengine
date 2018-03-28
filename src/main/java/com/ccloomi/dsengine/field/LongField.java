package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;

import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.util.BytesUtil;


/**@类名 LongField
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月30日-上午10:16:03
 */
public class LongField extends SchemaField{
	public LongField(String name,IndexAnalyze analyze){
		super(name);
		setLength(8);
		this.analyze=analyze;
	}
	
	@Override
	public void appendToByteBuffer(ByteBuffer bbuf, Object value) {
		if(value instanceof Long){
			bbuf.putLong((long)value);
		}else{
			bbuf.putLong(Long.valueOf((String)value));
		}
	}
	
	@Override
	public Object transformValue() {
		return BytesUtil.readBytesToLong(bytes, -1);
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
