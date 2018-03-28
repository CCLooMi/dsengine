package com.ccloomi.dsengine.query;

import java.util.Map;

import com.ccloomi.dsengine.tree.QueryTree;

/**@类名 FieldQuery
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月6日-下午6:27:31
 */
public class FieldQuery extends Query{
	public FieldQuery(Map<String, String[]>fieldKSMap,Map<String, QueryTree> mtree) {
		this.fieldKSMap=fieldKSMap;
		this.mtree=mtree;
	}
}
