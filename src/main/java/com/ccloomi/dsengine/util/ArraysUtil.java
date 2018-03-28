package com.ccloomi.dsengine.util;

/**@类名 ArraysUtil
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月13日-下午4:59:26
 */
public class ArraysUtil {
	/**
	 * @名称 reverse
	 * @说明	反转数组
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月13日-下午5:01:09
	 * @param t
	 */
	public static <T>void reverse(T[]t){
		int l=t.length/2;
		int al=t.length-1;
		for(int i=0;i<l;i++){
			T tp=t[i];
			t[i]=t[al-i];
			t[al-i]=tp;
		}
	}
}
