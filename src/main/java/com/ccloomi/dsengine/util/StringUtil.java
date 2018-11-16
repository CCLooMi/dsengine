package com.ccloomi.dsengine.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类名：StringUtil
 * 描述：字符串工具类
 * 作者： Chenxj
 * 日期：2015年6月18日 - 下午4:40:44
 */
public class StringUtil {
	/**
	 * 方法描述：字符串连接
	 * 作者：Chenxj
	 * 日期：2015年6月18日 - 下午5:20:32
	 * @param s
	 * @param strs
	 * @return StringBuilder
	 */
	public static StringBuilder join(String s,String...strs){
		StringBuilder sb=new StringBuilder();
		int l=strs.length;
		if(l>0){
			sb.append(strs[0]);
			for(int i=1;i<l;i++){
				sb.append(s).append(strs[i]);
			}
			return sb;
		}else{
			return sb;
		}
	}
	/**
	 * 方法描述：字符串连接
	 * 作者：Chenxj
	 * 日期：2015年6月18日 - 下午5:20:32
	 * @param s
	 * @param objs
	 * @return StringBuilder
	 */
	public static StringBuilder join(String s,Object...objs){
		return joinArray(s,objs);
	}
	public static StringBuilder joinArray(String s,Object[]objs){
		StringBuilder sb=new StringBuilder();
		int l=objs.length;
		if(l>0){
			Object obj0=objs[0];
			if(obj0 instanceof Object[]){
				sb.append('[').append(join(s,(Object[])obj0)).append(']');
			}else{
				sb.append(objs[0]);
			}
			for(int i=1;i<l;i++){
				Object obji=objs[i];
				if(obji instanceof Object[]){
					sb.append(s).append('[').append(join(s, (Object[])obji)).append(']');
				}else{
					sb.append(s).append(objs[i]);
				}
			}
			return sb;
		}else{
			return sb;
		}
	}
	public static StringBuilder joinCollection(String s,Collection<? extends Object>list){
		return joinArray(s,list.toArray());
	}
	/**
	 * 方法描述：连接字符串数组
	 * 作者：Chenxj
	 * 日期：2015年6月18日 - 下午5:22:01
	 * @param s
	 * @param objs
	 * @return String
	 */
	public static String joinString(String s,Object...objs){
		return join(s, objs).toString();
	}
	public static String joinArrayString(String s,Object[]objs){
		return joinArray(s,objs).toString();
	}
	public static String joinCollectionString(String s,Collection<? extends Object>list){
		return joinArray(s,list.toArray()).toString();
	}
	/**
	 * 方法描述：连接字符串数组
	 * 作者：Chenxj
	 * 日期：2015年6月18日 - 下午5:22:01
	 * @param s
	 * @param strs
	 * @return String
	 */
	public static String joinString(String s,String...strs){
		return join(s, strs).toString();
	}
	/**
	 * 描述：
	 * 作者：Chenxj
	 * 日期：2015年10月22日 - 下午9:32:13
	 * @return
	 */
	public static final String buildUUID() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}
	/**
	 * 方法描述：首字母大写
	 * 作        者：Chenxj
	 * 日        期：2015年11月20日-上午10:43:04
	 * @param str
	 * @return
	 */
	public static String upperCaseFirstLatter(String str){
		char[] strChar=str.toCharArray();
		strChar[0]-=32;
		return String.valueOf(strChar);
	}
	/**
	 * 方法描述：正则匹配
	 * 作        者：Chenxj
	 * 日        期：2016年5月24日-上午9:52:37
	 * @param regex
	 * @param str
	 * @return
	 */
    public static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.lookingAt();
    }
	public static long strtol(String str,int base){
		return strtol((str+"\0").toCharArray(),base);
	}
	public static long strtoul(String str,int base){
		return strtoul((str+"\0").toCharArray(),base);
	}
	public static String getFilePath(String basePath, String fileName, boolean autoCreateFile) {
		StringBuilder sb=new StringBuilder(basePath);
		String id=Integer.toHexString(fileName.hashCode());
		long v1=StringUtil.strtol(id.substring(0, 3), 16);
		long v2=StringUtil.strtol(id.substring(3, 6), 16);
		sb.append('/').append(v1/4).append('/').append(v2/4).append('/');
		if(autoCreateFile){
			File f=new File(sb.toString());
			if(!f.exists()){
				f.mkdirs();
			}
		}
		return sb.append(id).append('/').toString();
	}
	public static String getHashPathFile(String basePath, String path, boolean autoCreateFile) {
		StringBuilder sb = new StringBuilder(basePath.length()+path.length()+9);
		sb.append(basePath);
		String id=Integer.toHexString(path.hashCode());
		sb.append('/').append(id.substring(0, 3));
		sb.append('/').append(id.substring(3));
		if (autoCreateFile) {
			File f = new File(sb.toString());
			if (!f.exists()) {
				f.mkdirs();
			}
		}
		sb.append('/').append(path);
		return sb.toString();
	}
	/**
	 * @名称 baseScore
	 * @说明	计算字符串基础分
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年11月30日-下午4:59:48
	 * @param str
	 * @return
	 */
	public static float baseScore(String str) {
		if(str==null||str.length()==0)return 0;
		Set<Character>cset=new HashSet<>();
		int sl=0;
		for(int i=0;i<str.length();i++) {
			if(!cset.contains(str.charAt(i))) {
				sl++;
				cset.add(str.charAt(i));
			}
		}
		return (float) (sl-str.length()) / str.length();
	}
	//#####################################################################################
	//#
	//##
	//###                                以下所有为私有方法
	//##
	//#
	//#####################################################################################
	
	/**
	 * 描述：此方法只返回非负数
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:35:28
	 * @param cp
	 * @param base
	 * @return
	 */
	private static long strtoul(char[] cp,int base){
		long result=0,value;
		int i=0;
		if(base==0){
			base=10;
			if(cp[i]=='0'){
				base=8;
				i++;
				if(Character.toLowerCase(cp[i])=='x'&&isxdigit(cp[1])){
					i++;
					base=16;
				}
			}
		}else if(base==16){
			if(cp[0]=='0'&&Character.toLowerCase(cp[1])=='x')
				i+=2;
		}
		while(isxdigit(cp[i])&&(value = isdigit(cp[i]) ? cp[i]-'0' : Character.toLowerCase(cp[i])-'a'+10) < base){
			result=result*base+value;
			i++;
		}
		return result;
	}
	/**
	 * 描述：此会返回有符号数
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:36:08
	 * @param cp
	 * @param base
	 * @return
	 */
	private static long strtol(char[]cp,int base){
		if(cp[0]=='-'){
			return -strtoul(subChars(cp, 1),base);
		}
		return strtoul(cp, base);
	}
	/**
	 * 判断char是否是16进制以内的数
	 * @param c
	 * @return
	 */
	private static boolean isxdigit(char c){
		return ('0' <= c && c <= '9')||('a' <= c && c <= 'f')||('A' <= c && c <= 'F');
	}
	/**
	 * 判断char是否是10进制的数
	 * @param c
	 * @return
	 */
	private static boolean isdigit(char c){
		return '0' <= c && c <= '9';
	}
	/**
	 * 描述：char数组切分
	 * 作者：Chenxj
	 * 日期：2016年7月20日 - 下午10:37:59
	 * @param cp
	 * @param indexs
	 * @return
	 */
	private static char[] subChars(char[]cp,int...indexs){
		if(indexs.length==1){
			return Arrays.copyOfRange(cp, indexs[0], cp.length);
		}else if(indexs.length>1){
			return Arrays.copyOfRange(cp, indexs[0], indexs[0]+indexs[1]);
		}
		return cp;
	}
}
