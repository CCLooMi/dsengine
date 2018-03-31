package com.ccloomi.dsengine.analyze;

import java.util.Iterator;
import java.util.Map;

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
		Map<String, ?>cmap=value2Map(data);
		return cmap.keySet().toArray(new String[cmap.size()]);
	}
	protected abstract Map<String, ?> value2Map(Object value);
	@Override
	public String[][] difference(Object od, Object nw) {
		Map<String, ?>omap=value2Map(od);
		Map<String, ?>nmap=value2Map(nw);
		Iterator<String>it=omap.keySet().iterator();
		while(it.hasNext()) {
			String k=it.next();
			if(nmap.containsKey(k)) {
				it.remove();
				nmap.remove(k);
			}
		}
		return new String[][] {
			omap.keySet().toArray(new String[omap.size()]),
			nmap.keySet().toArray(new String[nmap.size()])};
	}
	@Override
	public String analyzeEL(String el) {
		Map<String, ?>cmap=value2Map(el);
		if(cmap.containsKey("2d")){// - 的HexString
			cmap.remove("2d");
			return StringUtil
					.joinCollection("&-", cmap.keySet())
					.insert(0, "-")
					.toString();
		}else{
			return StringUtil.joinCollectionString("&", cmap.keySet());
		}
	}
}
