package com.ccloomi.dsengine.bean;

import static com.ccloomi.dsengine.EngineConfigure.*;
import java.util.Arrays;
import java.util.Comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**@类名 ResultBean
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年2月7日-下午12:01:02
 */
public class ResultBean{
	/**结果ID，一般是查询语句的Hash值*/
	private String id;
	/**查询客户端随机生成的ID，用来标识是否是同一个查询的结果*/
	private String searchId;
	/**结果总数*/
	private long total;
	/**耗时(ms)*/
	private int took;
	/**命中结果*/
	private MapBean[]hits;

	private Comparator<MapBean>comparator;
	
	public ResultBean(){
		//从大到小排序
		this.comparator=new Comparator<MapBean>() {
			@Override
			public int compare(MapBean o1, MapBean o2) {
				if(o1==null&&o2==null){
					return 0;
				}else if(o1==null){
					return -1;
				}else if(o2==null){
					return 1;
				}else{
					float v1=o1.getScore();
					float v2=o2.getScore();
					if(v1>v2)return -1;
					if(v1<v2)return 1;
					return 0;
				}
			}
		};
	}
	public ResultBean combin(ResultBean resultBean){
		if(searchId.equals(resultBean.searchId)){
			total+=resultBean.total;
			MapBean[]hits2=resultBean.hits;
			int l1=hits.length;
			int l2=hits2.length;
			MapBean[]t_hits=new MapBean[topMax];
			if(topMax<(l1+l2)){
				int n=topMax-l1;
				System.arraycopy(hits, 0, t_hits, 0, l1);
				System.arraycopy(hits2, 0, t_hits, l1, n);
				Arrays.sort(t_hits, comparator);
				for(int i=n;i<l2;i++){
					MapBean tm=hits2[i];
					float t=tm.getScore();
					if(t>t_hits[topMax-1].getScore()){
						int j=topMax-2;
						for(;j>-1;j--){
							if(t_hits[j].getScore()<t){
								t_hits[j+1]=t_hits[j];
							}else{
								break;
							}
						}
						t_hits[j+1]=tm;
					}
				}
			}else{
				System.arraycopy(hits, 0, t_hits, 0, l1);
				System.arraycopy(hits2, 0, t_hits, l1, l2);
				Arrays.sort(t_hits, comparator);
			}
			this.hits=t_hits;
		}else{
			return resultBean;
		}
		return this;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSearchId() {
		return searchId;
	}
	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public int getTook() {
		return took;
	}
	public void setTook(int took) {
		this.took = took;
	}
	public MapBean[] getHits() {
		return hits;
	}
	public void setHits(MapBean[] hits) {
		this.hits = hits;
	}
	@Override
	public String toString() {
		ObjectMapper om=new ObjectMapper();
		try {
			return om
					.writerWithDefaultPrettyPrinter()
					.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			StringBuilder sb=new StringBuilder();
			sb.append("{took:").append(took).append("ms")
			.append(",total:").append(total)
			.append(",hits:").append(hits)
			.append('}');
			return sb.toString();
		}
	}
}
