package com.ccloomi.dsengine.tree;

/**© 2015-2018 Chenxj Copyright
 * 类    名：Tree
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午3:44:39
 */
public abstract class Tree<T> {
	/**ID*/
	protected Object id;
	/**左孩子节点*/
	protected Tree<T> lc;
	/**右孩子节点*/
	protected Tree<T> rc;
	protected boolean leaf=true;
	
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public Tree<T> getLc() {
		return lc;
	}
	public void setLc(Tree<T> lc) {
		this.lc = lc;
	}
	public Tree<T> getRc() {
		return rc;
	}
	public void setRc(Tree<T> rc) {
		this.rc = rc;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public String toString(){
		StringBuilder sb=new StringBuilder();
		if(lc!=null&rc!=null){
			sb.append(id+":{leftChild:"+lc+",rightChild:"+rc+"}");
		}else if(lc!=null){
			sb.append(id+":{leftChild:"+lc+"}");
		}else if(rc!=null){
			sb.append(id+":{rightChild:"+rc+"}");
		}else{
			sb.append(id);
		}
		return sb.toString();
	}
}
