package com.ccloomi.dsengine.bean;

import java.util.HashSet;
import java.util.Set;


/**@类名 FieldKS
 * @说明	文档打分需要用到这些属性，为了防止重复生成，故预先生成
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月12日-下午4:00:30
 */
public class FieldKS{
	/**查询字段名称*/
	private String fieldName;
	/**查询字段的值数组*/
	private String[] fieldValues;
	/**查询字段值Set*/
	private Set<Integer>[] fieldValueSet;
	
	@SuppressWarnings("unchecked")
	public FieldKS(String fieldName,String[]fieldValues){
		this.fieldName=fieldName;
		this.fieldValues=fieldValues;
		this.fieldValueSet=new Set[fieldValues.length];
		
		for(int i=0;i<fieldValues.length;i++){
			Set<Integer>set=new HashSet<>();
			for(int j=0;j<fieldValues[i].length();j++){
				//防止自动装箱以提高性能
				set.add(new Integer(fieldValues[i].charAt(j)));
			}
			fieldValueSet[i]=set;
		}
	}
	public boolean isInVset(int i,char c) {
		return fieldValueSet[i].contains(new Integer(c));
	}
	public String getFieldName() {
		return fieldName;
	}
	public String[] getFieldValues() {
		return fieldValues;
	}
	public int getFieldValuesl() {
		return fieldValues.length;
	}
}
