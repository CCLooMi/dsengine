package com.ccloomi.dsengine.analyze;

import java.util.HashMap;
import java.util.Map;

import com.ccloomi.dsengine.util.ArraysUtil;
import com.ccloomi.dsengine.util.StringUtil;

/**@类名 DateAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月23日-下午3:14:07
 */
public class DateAnalyze extends BaseAnalyze{
	private static final long serialVersionUID = 4894936490930681246L;

	@Override
	public String[] analyze(Object data) {
		String date=null;
		if(data instanceof String) {
			date=(String)data;
		}else {
			date=String.valueOf(data);
		}
		String[]result=new String[4];
		String[]dt=date.split("-");
		ArraysUtil.reverse(dt);
		result[0]="0_"+dt[0];
		result[1]="1_"+dt[1];
		result[2]="20_"+dt[2].substring(2, 4);
		result[3]="21_"+dt[2].substring(0, 2);
		return result;
	}
	
	@Override
	public String analyzeEL(String el) {
		return StringUtil.joinString("&", analyze(el));
	}

	@Override
	protected Map<String, ?> value2Map(Object value) {
		Map<String, ?>m=new HashMap<>();
		String[]ss=analyze(value);
		for(int i=0;i<ss.length;i++) {
			m.put(ss[i], null);
		}
		return m;
	}
}
