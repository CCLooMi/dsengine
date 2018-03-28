package com.ccloomi.dsengine.analyze;

/**@类名 AgeAnalyze
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2016年12月23日-下午4:18:59
 */
public class AgeAnalyze implements IndexAnalyze{
	private static final long serialVersionUID = 2154440468538242425L;

	@Override
	public String[] analyze(Object data) {
		String age=null;
		if(data instanceof String) {
			age=(String)data;
		}else {
			age=String.valueOf(data);
		}
		String[]result=new String[2];
		if(age.length()<2){
			result[0]="00";
			result[1]=age;
		}else if(age.length()<3){
			String[]as=age.split("");
			result[0]="0"+as[0];
			result[1]=as[1];
		}else{
			String[]as=age.split("");
			result[0]=as[0]+as[1];
			result[1]=as[2];
		}
		return result;
	}

	@Override
	public String analyzeEL(String el) {
		// TODO Auto-generated method stub
		return null;
	}
}
