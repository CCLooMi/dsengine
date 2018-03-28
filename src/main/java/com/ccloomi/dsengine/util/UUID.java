package com.ccloomi.dsengine.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**© 2015-2018 Chenxj Copyright
 * 类    名：UUID
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月26日-下午5:17:05
 */
public class UUID {
	public static byte[] randomUUID() {
		byte[]randomBytes=new byte[16];
		SecureRandom sr=new SecureRandom();
		sr.nextBytes(randomBytes);
		return randomBytes;
	}
	public static byte[] fromString(String uuid) {
		byte[]bytes=new byte[16];
		long a=new BigInteger(uuid.substring(0, 16), 16).longValue();
		long b=new BigInteger(uuid.substring(16),16).longValue();
		for(int i=0;i<8;i++) {
			bytes[i]=(byte) (0xff&(a>>(i<<3)));
		}
		for(int i=8,j=0;i<16;i++,j++) {
			bytes[i]=(byte) (0xff&(b>>(j<<3)));
		}
		return bytes;
	}
	public static String randomStringUUID() {
		return bytesToString(randomUUID());
	}
	public static String bytesToString(byte[]bs) {
		StringBuilder sb=new StringBuilder();
		long a=0,b=0;
		for(int i=0;i<8;i++) {
			a|=((long)bs[i]&0xff)<<(i<<3);
		}
		sb.append(Long.toHexString(a));
		for(int i=8,j=0;i<bs.length;i++,j++) {
			b|=((long)bs[i]&0xff)<<(j<<3);
		}
		sb.append(Long.toHexString(b));
		return sb.toString();
	}
	public static void main(String[] args) {
		String id=randomStringUUID();
		System.out.println(id);
		System.out.println(bytesToString(fromString(id)));
	}
}
