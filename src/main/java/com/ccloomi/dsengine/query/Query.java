package com.ccloomi.dsengine.query;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ccloomi.dsengine.bean.FieldKS;
import com.ccloomi.dsengine.tree.QueryTree;

/**© 2015-2018 Chenxj Copyright
 * 类    名：Query
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午2:57:07
 */
public abstract class Query {
	/**属性对应的关键字列表，用于文档打分*/
	protected Map<String, String[]>fieldKSMap;
	protected FieldKS[] fieldKSArray;
	protected Map<String, QueryTree> mtree;
	/**小于0表示不分页查询*/
	protected int page;
	protected int pageSize;
	public FieldKS[] getFieldKSArray(){
		if(fieldKSArray!=null){
			return fieldKSArray;
		}else{
			List<FieldKS>ls=new ArrayList<>();
			for(Entry<String, String[]>entry:fieldKSMap.entrySet()){
				ls.add(new FieldKS(entry.getKey(), entry.getValue()));
			}
			fieldKSArray=new FieldKS[ls.size()];
			ls.toArray(fieldKSArray);
			return fieldKSArray;
		}
	}

	public Map<String, String[]> getFieldKSMap() {
		return fieldKSMap;
	}

	public void setFieldKSMap(Map<String, String[]> fieldKSMap) {
		this.fieldKSMap = fieldKSMap;
	}

	public Map<String, QueryTree> getMtree() {
		return mtree;
	}

	public void setMtree(Map<String, QueryTree> mtree) {
		this.mtree = mtree;
	}

	public int page() {
		return page;
	}
	public Query page(int page) {
		this.page = page;
		return this;
	}
	public int pageSize() {
		return pageSize;
	}
	public Query pageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}
}
