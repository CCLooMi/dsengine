package com.ccloomi.dsengine.analyze;

import java.util.HashMap;
import java.util.Map;

/**@类名 DefaultAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月23日-下午2:55:37
 */
public class DefaultAnalyze extends BaseAnalyze {
	private static final long serialVersionUID = -7283639640435539056L;

	@Override
	protected Map<String, ?> value2Map(Object value) {
		Map<String, ?>map=new HashMap<>();
		char[]chars=null;
		if(value instanceof String) {
			chars=((String)value).toCharArray();
		}else {
			chars=String.valueOf(value).toCharArray();
		}
		for(int i=0;i<chars.length;i++) {
			map.put(Integer.toHexString((int)chars[i]), null);
		}
		return map;
	}
	
}
