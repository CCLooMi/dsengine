package t;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**© 2015-2018 Chenxj Copyright
 * 类    名：PTest
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年4月1日-上午11:15:43
 */
public class PTest {
	public static void main(String[] args) {
//		long docId=-10232321;
//		System.out.println(docId/8);
//		System.out.println(docId%8);
//		System.out.println(docId>>3);
//		System.out.println(docId>>>3);
//		System.out.println("================");6
//		byte a=0b01100000;
//		int b=0b10000000;
//		a&=~(b>>>1);
//		System.out.println(Integer.toBinaryString(a));
		
		ByteBuffer bb=ByteBuffer.allocate(16);
		bb.putInt(12);
		bb.flip();
		bb.compact();
		bb.clear();
		
		
		bb=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong((3l<<62)|1);
		bb.flip();
		
		int[]m0=new int[129];
		for(int i=0;i<8;i++) {
			m0[1<<i]=i;
		}
		
		byte[]bs=new byte[8];
		bb.get(bs);
		for(int i=0;i<bs.length;i++) {
			System.out.println(Integer.toBinaryString(bs[i]&0xff));
			
			
			if(bs[i]!=0) {
				int b=(bs[i]&0xff);
				while(b>0) {
					System.out.println("m0[b&-b]::"+m0[b&-b]);
					b-=(b&-b);
				}
			}
			
		}
		
	}
}
