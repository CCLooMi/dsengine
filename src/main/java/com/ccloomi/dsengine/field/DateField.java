package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.util.BytesUtil;


/**@类名 DateField
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月30日-上午10:23:09
 */
public class DateField extends SchemaField{
	private SimpleDateFormat sdf;
	public DateField(String name,IndexAnalyze analyze){
		super(name);
		setLength(8);
		this.analyze=analyze;
		this.sdf=new SimpleDateFormat("yyyy-MM-dd");
	}
	@Override
	public void appendToByteBuffer(ByteBuffer bbuf, Object value) {
		if(value instanceof String){
			try {
				Date date=sdf.parse((String)value);
				bbuf.putLong(date.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}else {
			bbuf.putLong((long)value);
		}
	}
	@Override
	public Object transformValue() {
		return BytesUtil.readBytesToLong(bytes,-1);
	}
	@Override
	public String transformValue(Object value) {
		if(value instanceof String){
			return (String)value;
		}else{
			return sdf.format((Date)value);
		}
	}
}
