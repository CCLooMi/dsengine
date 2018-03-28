package com.ccloomi.dsengine.analyze;

import java.util.HashSet;
import java.util.Set;

import com.ccloomi.dsengine.util.StringUtil;


/**@类名 EmailAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月26日-上午11:25:35
 */
public class EmailAnalyze implements IndexAnalyze{
	private static final long serialVersionUID = 4324407832463352072L;

	@Override
	public String[] analyze(Object data) {
		String[]ss=((String)data).split("@");
		Set<String>rset=new HashSet<>();
		char[]cs=ss[0].toCharArray();
		for(char c:cs){
			rset.add(String.valueOf(c));
		}
		ss=ss[1].split("\\.");
		
		cs=ss[0].toCharArray();
		for(char c:cs){
			rset.add(String.valueOf(c));
		}
		rset.add(ss[1]);
		if(ss.length>2){
			rset.add(ss[2]);
		}
		ss=new String[rset.size()];
		return rset.toArray(ss);
	}

	@Override
	public String analyzeEL(String el) {
		if(el.contains("@")){
			return StringUtil.joinArrayString("&", analyze(el));
		}else if(el.contains(".")){
			Set<String>rset=new HashSet<>();
			String[]ss=el.split("\\.");
			char[]cs=ss[0].toCharArray();
			for(char c:cs){
				rset.add(String.valueOf(c));
			}
			rset.add(ss[1]);
			if(ss.length>2){
				rset.add(ss[2]);
			}
			return StringUtil.joinCollectionString("&", rset);
		}else{
			Set<String>rset=new HashSet<>();
			char[]cs=el.toCharArray();
			for(char c:cs){
				rset.add(String.valueOf(c));
			}
			return StringUtil.joinCollectionString("&", rset);
		}
	}
}
