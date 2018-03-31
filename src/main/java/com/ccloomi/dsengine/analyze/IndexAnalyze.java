package com.ccloomi.dsengine.analyze;

import java.io.Serializable;

/**@类名 IndexAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月23日-下午2:50:40
 */
public interface IndexAnalyze extends Serializable{
	/**
	 * @名称 analyze
	 * @说明	用来分词
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2017年1月6日-下午3:46:10
	 * @param data
	 * @return
	 */
	public String[] analyze(Object data);
	/**
	 * 描述：计算两个值分词后的差异，更新文档时需要
	 * 作者：chenxj
	 * 日期：2018年3月31日 - 下午8:15:22
	 * @param od 老的值
	 * @param nw 新的值
	 * @return diff[0] 需要删除的keys，diff[1] 需要增加的keys
	 */
	public String[][] difference(Object od,Object nw);
	/**
	 * @名称 analyzeEL
	 * @说明	用来分析转换查询语句
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2017年1月6日-下午3:45:49
	 * @param el
	 * @return
	 */
	public String analyzeEL(String el);
}
