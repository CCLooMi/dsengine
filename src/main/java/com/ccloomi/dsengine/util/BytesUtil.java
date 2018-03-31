package com.ccloomi.dsengine.util;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**© 2015-2016 CCLooMi.Inc Copyright
 * 类    名：BytesUtil
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2016年6月25日-上午11:39:12
 */
public class BytesUtil {
	/**
	 * 描述：字节数组转整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:26:02
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static int readBytesToInt(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=(bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=(bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:25:22
	 * @param a 整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] intToBytes(int a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>(8*(length-1-i))&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转长整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:31:49
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static long readBytesToLong(byte[]bytes,int endianness){
		long a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：长整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:32:18
	 * @param a 长整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] longToBytes(long a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>((length-1-i)*8)&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转双精度类型
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:40:16
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static double readBytesToDouble(byte[]bytes,int endianness){
		return Double.longBitsToDouble(readBytesToLong(bytes,endianness));
	}
	/**
	 * 描述：双精度类型转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:42:16
	 * @param a 双精度类型
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] doubleToBytes(double a,int length,int endianness){
		return longToBytes(Double.doubleToLongBits(a), length,endianness);
	}
	/**
	 * 描述：字节数组转Float
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:30:53
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static float readBytesToFloat(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return Float.intBitsToFloat(a);
	}
	/**
	 * 描述：Float转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:31:24
	 * @param a Float
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] floatToBytes(float a,int length,int endianness){
		long b=Float.floatToIntBits(a);
		return longToBytes(b, length,endianness);
	}
	
	public final static byte[]writeValueAsBytes(Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	public final static void writeValueAsBytes(Object obj,byte[] b, int off,int len){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			System.arraycopy(bos.toByteArray(), 0, b, off, len);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
//	public final static byte[] writeValueAsBytes(Object obj,byte b0){
//		ByteArrayOutputStream bos=new ByteArrayOutputStream();
//		ObjectOutputStream oos=null;
//		try{
//			oos=new ObjectOutputStream(bos);
//			oos.writeObject(obj);
//			oos.flush();
//			byte[]bytes=new byte[bos.size()+1];
//			bytes[0]=b0;
//			System.arraycopy(bos.toByteArray(), 0, bytes, 1, bos.size());
//			return bytes;
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		return new byte[0];
//	}
	public final static <E>E readBytesAsObject(byte[]buf){
		if(buf!=null){
			return readBytesAsObject(buf, 0, buf.length);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public final static <E>E readBytesAsObject(byte buf[], int offset, int length){
		ByteArrayInputStream byteInStream=new ByteArrayInputStream(buf, offset, length);
		ObjectInputStream ois=null;
		try{
			ois=new ObjectInputStream(byteInStream);
			return (E) ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 描述：两个字节数组相与运算
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 下午5:19:36
	 * @param a
	 * @param b
	 * @return
	 */
	public static short aANDb(byte[]a,byte[]b){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			if(a[i]==0){
				x0++;
			}else if(a[i]==-1){
				a[i]=b[i];
				x1++;
			}else if(a[i]!=b[i]){
				a[i]&=b[i];
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
	/**
	 * 描述：两个字节数组相或运算
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 下午5:20:03
	 * @param a
	 * @param b
	 * @return
	 */
	public static short aORb(byte[]a,byte[]b){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			if(a[i]==0){
				a[i]=b[i];
				x0++;
			}else if(a[i]==-1){
				x1++;
			}else if(a[i]!=b[i]){
				a[i]|=b[i];
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
	/**
	 * 描述：反转byte[]
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 上午9:58:54
	 * @param a
	 * @return
	 */
	public static short revers(byte[]a){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			a[i]=(byte) ~a[i];
			if(a[i]==0){
				x0++;
			}else if(a[i]==-1){
				x1++;
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
}
