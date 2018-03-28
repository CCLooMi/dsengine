package com.ccloomi.dsengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ccloomi.dsengine.field.SchemaField;

/**© 2015-2018 Chenxj Copyright
 * 类    名：Schema
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午2:40:55
 */
public class Schema {
	private String name;
	private List<SchemaField>fields;
	private SchemaField[]fieldsArray;
	private Map<String, SchemaField>fieldMap;
	private boolean fieldChange;
	private MapBeanPool mbpool;
	
	public Schema(String name) {
		this.name=name;
		this.fields=new ArrayList<>();
		this.fieldMap=new HashMap<>();
		this.fieldChange=false;
		this.mbpool=new MapBeanPool();
	}
	public Schema addField(SchemaField field) {
		fields.add(field);
		fieldMap.put(field.getName(), field);
		fieldChange=true;
		return this;
	}
	public SchemaField[] getFieldsArray() {
		if(fieldChange||fieldsArray==null) {
			fieldsArray=new SchemaField[fields.size()];
			fields.toArray(fieldsArray);
			fieldChange=false;
		}
		return fieldsArray;
	}
	public SchemaField getSchemaField(String fieldName){
		return fieldMap.get(fieldName);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public MapBeanPool getMbpool() {
		return mbpool;
	}
}
