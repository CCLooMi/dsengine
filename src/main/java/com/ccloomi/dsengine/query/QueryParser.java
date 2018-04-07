package com.ccloomi.dsengine.query;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.ccloomi.dsengine.Schema;
import com.ccloomi.dsengine.analyze.IndexAnalyze;
import com.ccloomi.dsengine.field.SchemaField;
import com.ccloomi.dsengine.tree.QueryTree;
import com.ccloomi.dsengine.util.StringUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**@类名 QueryParser
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月6日-上午11:45:56
 */
public class QueryParser {
	protected static ObjectMapper objectMapper=new ObjectMapper();
	protected static JsonFactory jsonFactory=new JsonFactory()
			.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(Feature.ALLOW_SINGLE_QUOTES);

	
	@SuppressWarnings("unchecked")
	public static Query parser(Schema schema,String el) {
		Map<String, String>m=new HashMap<>();
		try {
			JsonParser jp=jsonFactory.createParser(el);
			m=objectMapper.readValue(jp, Map.class);
		}catch (Exception e) {
		}
		return parser(schema, m);
	}
	/**
	 * 根据schema解析查询表达式
	 * @param schema
	 * @param el
	 * @return
	 */
	public static Query parser(Schema schema,Map<String, String>qm){
		//属性和对应关键字MAP
		Map<String, String[]>fieldKSMap=new HashMap<>();
		Map<String, QueryTree> mtree=new HashMap<>();
		for(Entry<String, String>entry:qm.entrySet()){
			SchemaField field=schema.getSchemaField(entry.getKey());
			if(field!=null){
				String value=entry.getValue();
				if(value==null||"".equals(value)){
					continue;
				}
				
				IndexAnalyze fieldAnalyze=field.getAnalyze();
				String[]data=value.split(" +");
				int datal=data.length;
				
				if(field.isScoreable()){
					//用于匹配度打分
					fieldKSMap.put(entry.getKey(), data);
				}
				StringBuilder sb=new StringBuilder();
				List<String>ls=new ArrayList<>();
				List<String>_ls=new ArrayList<>();
				for(int i=0;i<datal;i++){
					if(data[i].startsWith("-")){
						_ls.add(fieldAnalyze.analyzeEL(data[i]));
					}else{
						ls.add(fieldAnalyze.analyzeEL(data[i]));
					}
				}
				if(_ls.size()>0){
					if(ls.size()>0){
						if(ls.size()>1){
							sb.append("{").append(StringUtil.joinCollection("|", ls)).append("}");
						}else{
							sb.append(ls.get(0));
						}
						sb.append("&");
					}
					if(_ls.size()>1){
						sb.append("{").append(StringUtil.joinCollection("|", _ls)).append("}");
					}else{
						sb.append(_ls.get(0));
					}
				}else{
					sb.append(StringUtil.joinCollection("|", ls));
				}
				entry.setValue(sb.toString());
				mtree.put(entry.getKey(), transformELTree(entry.getKey(),transform(sb.toString())));
			}
		}
		return new FieldQuery(fieldKSMap, mtree);
	}
	
	/**
	 * @名称 precedence
	 * @说明	定义运算符优先级 越小优先级越高
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2017年1月4日-上午10:10:10
	 * @param sign
	 * @return
	 */
	private static int precedence(char sign){
		switch (sign) {
		case '&':
			return 1;
		case '|':
			return 2;
		case '{':
		case '}':
		default:
			return 0;
		}
	}
	/**
	 * 描述：判断是否是操作符
	 * 作者：Chenxj
	 * 日期：2017年1月4日 - 下午9:42:42
	 * @param c
	 * @return
	 */
	private static boolean isSign(char c){
		switch (c) {
		case '&':
		case '|':
		case '{':
		case '}':
			return true;
		default:
			return false;
		}
	}

	/**
	 * 描述：将表达式转换为逆波兰表达式
	 * 作者：Chenxj
	 * 日期：2017年1月4日 - 下午9:43:06
	 * @param el
	 * @return
	 */
	private static Stack<Object> transform(String el){
		Stack<Object>operand=new Stack<>();
		Stack<Character>operator=new Stack<>();
		//操作数数组
		String[]ods=el.replaceAll("\\{|\\}|\\||\\&", " ").split(" +");
		int odsIndex=0;
		char[]chars=el.toCharArray();
		int l=chars.length;
		for(int i=0;i<l;i++){
			char c=chars[i];
			if(!isSign(c)){
				String od=ods[odsIndex++];
				if(!"".equals(od)){
					operand.push(od);
				}
				i+=od.length()-1;
			}else{
				if(c=='{'){
					operator.add(c);
				}else if(c=='}'){
					char c2=operator.pop();
					while(c2!='{'){
						operand.add(c2);
						if(!operator.isEmpty()){
							c2=operator.pop();
						}else{
							break;
						}
					}
				}else {
					if(operator.isEmpty()){
						operator.add(c);
					}else if(operator.peek()=='{'){
						operator.add(c);
					}else if(precedence(c)<precedence(operator.peek())){
						operator.add(c);
					}else{
						while(!operator.isEmpty()){
							char c2=operator.peek();
							if(precedence(c)>=precedence(c2)&&c2!='{'){
								operand.add(c2);
								operator.pop();
							}else{
								break;
							}
						}
						operator.add(c);
					}
				}
			}
		}
		while(!operator.isEmpty()){
			operand.add(operator.pop());
		}
		return operand;
	}
	private static QueryTree transformELTree(String fieldName,Stack<Object> operand) {
		int oal=operand.size();
		Stack<QueryTree> stack = new Stack<>();
		for (int i=0;i<oal;i++) {
			Object o=operand.get(i);
			if (o instanceof Character) {
				QueryTree tree = new QueryTree(o, false);
				tree.setRc(stack.pop());
				tree.setLc(stack.pop());
				stack.add(tree);
			} else {
				stack.add(new QueryTree(o,true));
			}
		}
		return stack.peek();
	}
}
