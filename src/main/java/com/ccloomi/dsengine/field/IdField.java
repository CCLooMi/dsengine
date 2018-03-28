package com.ccloomi.dsengine.field;

import java.nio.ByteBuffer;

import com.ccloomi.dsengine.util.UUID;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IdField
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月26日-下午4:42:23
 */
public class IdField extends SchemaField{

	public IdField(String name) {
		super(name);
		setLength(16);
		//ID 不建索引
		this.indexable=false;
	}

	@Override
	public void appendToByteBuffer(ByteBuffer bbuf, Object value) {
		byte[]bs=toBytes(value);
		if(bs!=null) {
			bbuf.put(bs);
		}
	}
	@Override
	public byte[] toBytes(Object value) {
		if(value instanceof String) {
			return UUID.fromString((String)value);
		}else if(value instanceof byte[]) {
			return (byte[])value;
		}
		return null;
	}
	@Override
	public Object transformValue() {
		return UUID.bytesToString(bytes);
	}

	@Override
	public String transformValue(Object value) {
		return null;
	}

}
