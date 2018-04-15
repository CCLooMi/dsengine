package com.ccloomi.dsengine.linkthread;

import static com.ccloomi.dsengine.EngineConfigure.topMax;

import java.util.Arrays;
import java.util.Comparator;

import com.ccloomi.dsengine.DataAccess;
import com.ccloomi.dsengine.bean.MapBean;
import com.ccloomi.dsengine.query.Query;


/**@类名 Sort
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-下午1:52:15
 */
public class Sort extends BaseLinkedThread<MapBean, MapBean[]>{
	
	private DataAccess da;
	
	private MapBean[]docs;
	private Comparator<MapBean>baseSort;
	private int docCount=0;
	private int max;
	
	public Sort(DataAccess da){
		this.setName("LinkThread_C-Document Sort");
		this.da=da;
		this.baseSort=new Comparator<MapBean>() {
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
					if(v1>v2)return 1;
					if(v1<v2)return -1;
					return 0;
				}
			}
		};
	}
	@Override
	public void setQuery(Query query) {
		super.setQuery(query);
		if(query.page()<0) {
			max=topMax;
		}else {
			max=(query.page()+1)*query.pageSize();
			if(max>topMax) {
				max=topMax;
			}
		}
		this.docs=new MapBean[max];
	}
	@Override
	public MapBean[] processData(MapBean t) {
		if(docCount<max){//数据填充阶段
			docs[docCount]=t;
		}else if(docCount==max){//数据准备分界点
			Arrays.sort(docs,baseSort);
			if(t.getScore()>docs[0].getScore()){
				//回收map对象
				da.recycMapBean(docs[0]);
				//对已排序数组没有必要两两交换，只需要所有比t小的数据左移动就可以了
				int i=1;
				for(;i<max;i++){
					if(docs[i].getScore()<t.getScore()){
						docs[i-1]=docs[i];
					}else{
						break;
					}
				}
				docs[i-1]=t;
			}else{
				//回收Map对象
				da.recycMapBean(t);
			}
		}else{//数据查找阶段
			if(t.getScore()>docs[0].getScore()){
				//回收map对象
				da.recycMapBean(docs[0]);
				//对已排序数组没有必要两两交换，只需要所有比t小的数据左移动就可以了
				int i=1;
				for(;i<max;i++){
					if(docs[i].getScore()<t.getScore()){
						docs[i-1]=docs[i];
					}else{
						break;
					}
				}
				docs[i-1]=t;
			}else {
				//回收Map对象
				da.recycMapBean(t);
			}
		}
		docCount++;
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E>E get(){
		MapBean[]result=null;
		try{
			loop:while(true){
				if(isComplete()){
					Arrays.sort(docs,baseSort);
					if(query.page()<0) {
						if(docCount<max){
							result=new MapBean[docCount];
							for(int i=max-docCount,j=0;i<max;i++){
								result[j++]=docs[i];
							}
						}else{
							result=new MapBean[max];
							for(int i=0;i<max;i++){
								result[i]=docs[i];
							}
						}
					}else {
						if(docCount<=query.page()*query.pageSize()) {
							result= new MapBean[0];
							//不直接返回是因为需要调用后面的reset方法，
							//如果直接返回，则需要在返回之前reset
							break loop;
						}else {
							if(max<topMax) {
								if(docCount<max) {
									int remain=docCount%query.pageSize();
									result=new MapBean[remain];
									for(int i=query.pageSize()-remain,j=0;i<query.pageSize();i++) {
										result[j++]=docs[i];
									}
								}else {
									result=new MapBean[query.pageSize()];
									for(int i=0;i<query.pageSize();i++) {
										result[i]=docs[i];
									}
								}
							}else {
								if(docCount<max) {
									int total=docCount/query.pageSize();
									if(query.page()<total) {
										result=new MapBean[query.pageSize()];
										for(int i=0;i<query.pageSize();i++) {
											result[i]=docs[i];
										}
									}else {
										int remain=docCount%query.pageSize();
										result=new MapBean[remain];
										for(int i=query.pageSize()-remain,j=0;i<query.pageSize();i++) {
											result[j++]=docs[i];
										}
									}
								}else {
									int total=max/query.pageSize();
									if(query.page()<total) {
										result=new MapBean[query.pageSize()];
										for(int i=0;i<query.pageSize();i++) {
											result[i]=docs[i];
										}
									}else {
										int remain=max%query.pageSize();
										result=new MapBean[remain];
										for(int i=query.pageSize()-remain,j=0;i<query.pageSize();i++) {
											result[j++]=docs[i];
										}
									}
								}
							}
						}
					}
					break;
				}
				sleep(1);
			}
		}catch (Exception e) {
			e.printStackTrace();
			result=new MapBean[0];
		}
		this.reset();
		return (E) result;
	}
	
	@Override
	public void reset() {
		this.docCount=0;
		this.complete=false;
		for(int i=0;i<max;i++){
			docs[i]=null;
		}
	}
}
