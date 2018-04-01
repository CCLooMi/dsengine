package t;

import java.nio.ByteBuffer;

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
	}
}
