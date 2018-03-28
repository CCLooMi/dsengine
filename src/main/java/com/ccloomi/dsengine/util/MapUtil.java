package com.ccloomi.dsengine.util;

import java.util.HashMap;
import java.util.Map;

/**© 2015-2016 CCLooMi.Inc Copyright
 * 类    名：MapUtil
 * 类 描 述：Map工具类
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2016年4月24日-下午5:06:18
 */
public class MapUtil {
	@SafeVarargs
	public static <T>Map<String, T>map(T...tt){
		Map<String, T>map=new HashMap<>();
		for(int i=0;i<tt.length;i++){
			if(i%2!=0){
				map.put(String.valueOf(tt[i-1]), tt[i]);
			}
		}
		return map;
	}
	@SafeVarargs
	public static <T>Map<String, Object>objectMap(T...tt){
		Map<String, Object>map=new HashMap<>();
		for(int i=0;i<tt.length;i++){
			if(i%2!=0){
				map.put(String.valueOf(tt[i-1]), tt[i]);
			}
		}
		return map;
	}
	@SafeVarargs
	public static <T>Map<String, String>stringMap(T...tt){
		Map<String, String>map=new HashMap<>();
		for(int i=0;i<tt.length;i++){
			if(i%2!=0){
				if(tt[i]==null){
					map.put(String.valueOf(tt[i-1]), null);
				}else{
					map.put(String.valueOf(tt[i-1]), String.valueOf(tt[i]));
				}
			}
		}
		return map;
	}
}
