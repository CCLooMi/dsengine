package com.ccloomi.dsengine.linkthread;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**© 2015-2016 CCLooMi.Inc Copyright
 * 类    名：CCLinkedThread
 * 类 描 述：异步线程链（DT处理数据类型，RT返回数据类型）
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2016年11月28日-下午11:17:00
 */
public abstract class CCLinkedThread<DT,RT> extends Thread{
	private Queue<DT>dataPool;
	/**返回结果时需要监控此值*/
	protected boolean complete;
	protected long total;
	protected long current;
	protected CCLinkedThread<RT,? extends Object>nextThread;
	public CCLinkedThread() {
		this.dataPool=new LinkedBlockingQueue<>();
		this.complete=false;
		this.total=0;
		this.current=0;
		//设置为守护线程，主线程退出后此线程也会跟着退出
		this.setDaemon(true);
		this.start();
	}
	public CCLinkedThread<DT,RT> addData(DT t){
		this.dataPool.add(t);
		return this;
	}
	public void setTotal(long total){
		this.total=total;
	}
	public CCLinkedThread<DT,RT> complete(){
		this.complete=true;
		if(nextThread!=null){
			nextThread.setTotal(total);
		}
		return this;
	}
	public boolean isComplete(){
		return this.complete;
	}
	//当前线程的输出是下一线程的输入
	public <RT1>CCLinkedThread<RT,RT1> setNextThread(CCLinkedThread<RT,RT1>nextThread){
		this.nextThread=(CCLinkedThread<RT, ? extends Object>) nextThread;
		return nextThread;
	}
	public abstract RT processData(DT t);
	/**
	 * @名称 freeResources
	 * @说明	释放线程占用资源
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2016年12月14日-上午10:02:02
	 */
	public abstract void freeResources();
	//需要子类重写
	public RT segmentData(){
		return null;
	};
	public <T>T get(){
		if(nextThread!=null){
			return nextThread.get();
		}
		return null;
	};
	@Override
	public void run(){
//		try{
//			while(true){
//				if(!dataPool.isEmpty()){
//					while(!dataPool.isEmpty()){
//						if(nextThread!=null){
//							RT data=processData(dataPool.poll());
//							if(data!=null){
//								nextThread.addData(data);
//							}else{
//								//保证线程可以退出
//								nextThread.current++;
//							}
//						}else{
//							processData(dataPool.poll());
//						}
//						this.current++;
//					}
//				}else{
//					//在添加数据时total不能++，否则每处理完一条数据current++之后可能会和total相等
//					//total<0表示没有数据处理，直接退出
//					if((total>0&&total==current)||total<0){
//						this.complete();
//						this.total=0;
//						this.current=0;
//						freeResources();
//						//不需要退出线程
////						break;
//					}
//				}
//				sleep(1);
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
		//防止处理过程出错时线程退出导致无法进行下一个搜索
		try{
			while(true){
				if(!dataPool.isEmpty()){
					while(!dataPool.isEmpty()){
						if(nextThread!=null){
							RT data=null;
							try{data=processData(dataPool.poll());}
							catch (Exception e) {e.printStackTrace();}
							if(data!=null){
								nextThread.addData(data);
							}else{
								//保证线程可以退出
								nextThread.current++;
							}
						}else{
							try{processData(dataPool.poll());}
							catch(Exception e){e.printStackTrace();};
						}
						this.current++;
					}
				}else{
					//在添加数据时total不能++，否则每处理完一条数据current++之后可能会和total相等
					//total<0表示没有数据处理，直接退出
					if((total>0&&total==current)||total<0){
						this.complete();
						this.total=0;
						this.current=0;
						freeResources();
						//不需要退出线程
//						break;
					}
				}
				sleep(1);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
