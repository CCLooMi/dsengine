package com.ccloomi.dsengine.tree;

/**© 2015-2018 Chenxj Copyright
 * 类    名：QueryTree
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午11:12:05
 */
public class QueryTree extends Tree<byte[]>{
	public QueryTree(Object id,boolean leaf) {
		this.leaf=leaf;
		this.id=id;
	}
}
