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
	/**查询字段每个值对应的char数组*/
	private char[][] fieldValueChars;
	/**查询字段值Set*/
	private Set<Integer>[] fieldValueSet;
	/**查询字段值数组长度*/
	private int fieldValuesl;
	
	@SuppressWarnings("unchecked")
	public FieldKS(String fieldName,String[]fieldValues){
		this.setFieldName(fieldName);
		this.fieldValues=fieldValues;
		this.fieldValuesl=fieldValues.length;
		this.fieldValueChars=new char[fieldValuesl][];
		this.fieldValueSet=new Set[fieldValuesl];
		
		for(int i=0;i<fieldValuesl;i++){
			fieldValueChars[i]=fieldValues[i].toCharArray();
			int fvcl=fieldValueChars[i].length;
			Set<Integer>set=new HashSet<>();
			for(int j=0;j<fvcl;j++){
				//防止自动装箱以提高性能
//				set.add(new Character(fieldValueChars[i][j]));
				set.add(new Integer(fieldValueChars[i][j]));
			}
			fieldValueSet[i]=set;
		}
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String[] getFieldValues() {
		return fieldValues;
	}
	public void setFieldValues(String[] fieldValues) {
		this.fieldValues = fieldValues;
	}
	public char[][] getFieldValueChars() {
		return fieldValueChars;
	}
	public void setFieldValueChars(char[][] fieldValueChars) {
		this.fieldValueChars = fieldValueChars;
	}
	public int getFieldValuesl() {
		return fieldValuesl;
	}
	public void setFieldValuesl(int fieldValuesl) {
		this.fieldValuesl = fieldValuesl;
	}
	public Set<Integer>[] getFieldValueSet() {
		return fieldValueSet;
	}
	public void setFieldValueSet(Set<Integer>[] fieldValueSet) {
		this.fieldValueSet = fieldValueSet;
	}
}
