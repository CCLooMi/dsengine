package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;
import static com.ccloomi.dsengine.util.BytesUtil.aANDb;
import static com.ccloomi.dsengine.util.BytesUtil.aORb;

import java.util.Map.Entry;

import com.ccloomi.dsengine.query.Query;
import com.ccloomi.dsengine.tree.QueryTree;
import com.ccloomi.dsengine.tree.Tree;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IndexReader
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午5:51:50
 */
public class IndexReader {
	private byte[]bb=new byte[searchReadBufferSize];
	private DataAccess da;
	public short calculate(String filePath,Query query,long position,byte[]bytes) {
		int count=0;
		//i为-2表示关键字不存在
		short i=-1;
		for(Entry<String, QueryTree>entry:query.getMtree().entrySet()){
			QueryTree tree=entry.getValue();
			if(count++==0){
				i=calculate(filePath+"/"+entry.getKey(),tree,position,bytes);
			}else if(i==0){
				break;
			}else if(i==1||i==-2){
				i=calculate(filePath+"/"+entry.getKey(),tree,position,bytes);
			}else if(i==-1){
				if(calculate(filePath+"/"+entry.getKey(),tree,position,bb)!=-2){
					i=aANDb(bytes, bb);
				}
			}
		}
		return i;
	}
	
	public short calculate(String filePath,Tree<byte[]> tree,long position,byte[]bytes) {
		if(tree.isLeaf()){
			return da.readIndexData(filePath,(String)tree.getId(),position,bytes);
		}else if((char)tree.getId()=='&'){
			short i=calculate(filePath,tree.getLc(),position,bytes);
			if(i==0){
				return i;
			}else if(i==1){
				return calculate(filePath,tree.getRc(),position,bytes);
			}else {
				calculate(filePath,tree.getRc(),position,bb);
				return aANDb(bytes, bb);
			}
		}else{
			short i=calculate(filePath,tree.getLc(),position,bytes);
			if(i==0){
				return calculate(filePath,tree.getRc(),position,bytes);
			}else if(i==1){
				return i;
			}else{
				calculate(filePath,tree.getRc(),position,bb);
				return aORb(bytes, bb);
			}
		}
	}
	
	public void setDataAccess(DataAccess da) {
		this.da = da;
	}
	
}
