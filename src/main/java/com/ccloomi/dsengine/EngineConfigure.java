package com.ccloomi.dsengine;

/**© 2015-2018 Chenxj Copyright
 * 类    名：EngineConfigure
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午3:50:06
 */
public class EngineConfigure {
	/**搜索结果最大显示数量*/
	public static final int topMax=1024;
	/**搜索时读取索引文件缓存大小<br>缓存不可过大也不能太小，
	 * 过小则IO太过频繁，
	 * 过大则不能跳过大部分读取IO*/
	public static final int searchReadBufferSize=1024*32;
	/**最多搜索文档率，每次查询超过这个数将停止搜索*/
	public static final float searchMaxRate=.1f;
}
