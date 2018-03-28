package com.ccloomi.dsengine.linkthread;

import com.ccloomi.dsengine.Schema;
import com.ccloomi.dsengine.bean.FieldKS;
import com.ccloomi.dsengine.bean.MapBean;

/**@类名 Score
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-下午1:52:03
 */
public class Score  extends BaseLinkedThread<MapBean, MapBean>{
	public Score(Schema schema){
		this.setName("LinkThread_B-Document Score");
		this.schema=schema;
	}
	@Override
	public MapBean processData(MapBean t) {
		t.setScore(score(t));
		return t;
	}
	@Override
	public void reset() {
		
	}
	private float score(MapBean t){
		float score=0;
		FieldKS[]fkss=query.getFieldKSArray();
		int fkssl=fkss.length;
		for(int i=0;i<fkssl;i++){
			int fvsl=fkss[i].getFieldValuesl();
			char[]strChars=((String)t.getAttr(fkss[i].getFieldName()))
					.toCharArray();
			int strl=strChars.length;
			for(int j=0;j<fvsl;j++){
				//每个关键字对应的char数组
				char[]fvsc=fkss[i].getFieldValueChars()[j];
				int fvscl=fvsc.length;
				//带-号的不打匹配分，因为没有意义
				if(fvsc[0]=='-')break;
				int N=strl;
				int n=fvscl;
				if(n>N){N^=n;n^=N;N^=n;}
				//得分
				float scc=0;
				//字词的数量
				int m=0;
				//和关键字连词匹配的长度
				int k=0;
				int Nn=N-n+1;
				//根据两个字符串长度的差异来分别执行不同的打分算法可大幅提升性能
				if(Nn>n){
					if(strl>fvscl){
						for(int ii=0;ii<Nn;ii++){
							for(int jj=0;jj<n;jj++){
								if(strChars[jj+ii]==fvsc[jj]){
									m++;k++;
								}else{
									if(k>1){
										scc+=k-1;
									}
									k=0;
								}
							}
						}
					}else{
						for(int ii=0;ii<Nn;ii++){
							for(int jj=0;jj<n;jj++){
								if(fvsc[jj+ii]==strChars[jj]){
									m++;k++;
								}else{
									if(k>2){
										scc+=k-1;
									}
									k=0;
								}
							}
						}
					}
					if(k>1){
						scc+=k-1;
					}
				}else{
					k=0;
					//计算匹配分
					for(int jj=0;jj<strl;jj++){
						if(fkss[i].getFieldValueSet()[j].contains(new Integer(strChars[jj]))){
							m++;
							k++;
							if(jj>0){
								int a=fkss[i].getFieldValues()[j].indexOf(strChars[jj]);
								int b=fkss[i].getFieldValues()[j].indexOf(strChars[jj-1]);
								if(a-b==1&&b!=-1){
									scc++;
								}else{
									k=0;
								}
							}
							if(k>2){
								scc++;
							}
						}
					}
				}
				//第一个字符相同权值更高
				if(strl>0&&strChars[0]==fvsc[0]){
					m++;
				}
				scc+=(float)m/strl;
				score+=t.getAttrScore(fkss[i].getFieldName())+scc;
			}
		}
		return score;
	}
}
