package com.ccloomi.dsengine.linkthread;

import com.ccloomi.dsengine.Schema;
import com.ccloomi.dsengine.query.Query;

/**@类名 BaseLinkedThread
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月11日-上午10:36:11
 */
public abstract class BaseLinkedThread<DT, RT> extends CCLinkedThread<DT, RT>{
	protected Schema schema;
	protected Query query;
	public void freeResources() {
		
	}
	
	@SuppressWarnings("unchecked")
	public void setQuery(Query query){
		this.query=query;
		if(nextThread!=null&&nextThread instanceof BaseLinkedThread){
			((BaseLinkedThread<DT, RT>)nextThread).setQuery(query);
		}
	}
	/**
	 * @名称 reset
	 * @说明	重置线程环境
	 * @作者 Chenxj
	 * @邮箱 chenios@foxmail.com
	 * @日期 2017年1月17日-上午10:19:26
	 */
	public abstract void reset();
}
