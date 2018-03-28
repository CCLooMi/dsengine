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
