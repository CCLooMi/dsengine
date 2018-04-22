package com.ccloomi.dsengine.linkthread;

import com.ccloomi.dsengine.query.Query;

/**@类名 BaseLinkedThread
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月11日-上午10:36:11
 */
public abstract class BaseLinkedThread<DT, RT> extends CCLinkedThread<DT, RT>{
	protected Query query;
	public void freeResources() {}
	@Override
	public void onComplete() {}
	@SuppressWarnings("unchecked")
	public void setQuery(Query query){
		this.query=query;
		if(nextThread!=null&&nextThread instanceof BaseLinkedThread){
			((BaseLinkedThread<DT, RT>)nextThread).setQuery(query);
		}
	}
}
