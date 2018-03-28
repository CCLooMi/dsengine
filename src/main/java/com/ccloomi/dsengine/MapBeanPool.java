package com.ccloomi.dsengine;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import com.ccloomi.dsengine.bean.MapBean;



/**@类名 MapBeanPool
 * @说明	MapBeanPool
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-下午2:19:22
 */
public class MapBeanPool implements Serializable{
	private static final long serialVersionUID = -3237544088688675303L;
	private final Queue<MapBean> pool=new LinkedBlockingDeque<>();
	public MapBean get(){
		if(!pool.isEmpty()){
			return pool.poll();
		}
		return new MapBean();
	}
	public void recyc(MapBean docs){
		pool.add(docs);
	}
}
