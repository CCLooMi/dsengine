package com.ccloomi.dsengine.analyze;

import java.util.HashSet;
import java.util.Set;

import com.ccloomi.dsengine.util.StringUtil;

/**@类名 BaseAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月6日-下午3:49:38
 */
public abstract class BaseAnalyze implements IndexAnalyze{
	private static final long serialVersionUID = -4904509158665873373L;

	@Override
	public String[] analyze(Object data) {
		char[]chars=null;
		if(data instanceof String) {
			chars=((String)data).toCharArray();
		}else {
			chars=String.valueOf(data).toCharArray();
		}
		int length=chars.length;
		Set<String>cset=new HashSet<>(length);
		for(int i=0;i<length;i++){
			cset.add(Integer.toHexString((int)chars[i]));
		}
		return cset.toArray(new String[cset.size()]);
	}

	@Override
	public String analyzeEL(String el) {
		char[]chars=el.toCharArray();
		int length=chars.length;
		Set<String>cset=new HashSet<>(length);
		for(int i=0;i<length;i++){
			cset.add(Integer.toHexString((int)chars[i]));
		}
		if(cset.contains("2d")){// - 的HexString
			cset.remove("2d");
			return StringUtil
					.joinCollection("&-", cset)
					.insert(0, "-")
					.toString();
		}else{
			return StringUtil.joinCollectionString("&", cset);
		}
	}
}
