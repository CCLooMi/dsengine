package com.ccloomi.dsengine;

import static com.ccloomi.dsengine.EngineConfigure.searchReadBufferSize;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ccloomi.dsengine.query.Query;
import com.ccloomi.dsengine.tree.QueryTree;
import com.ccloomi.dsengine.tree.Tree;
import com.ccloomi.dsengine.util.StringUtil;

/**© 2015-2018 Chenxj Copyright
 * 类    名：IndexReader
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月24日-下午5:51:50
 */
public class IndexReader {
	private Map<Long, MappedByteBuffer>fileMBB;
	private byte[]bb=new byte[searchReadBufferSize];
	public IndexReader() {
		this.fileMBB=new HashMap<>();
	}
	
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
			return getBytes(filePath,(String)tree.getId(),position,bytes);
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
	
	protected short getBytes(String filePath,String fname,long position,byte[]bytes){
		if(fname.charAt(0)=='-'){
			MappedByteBuffer mbb=getMBB(filePath,fname.substring(1, fname.length()),position);
			if(mbb!=null){
				int l=searchReadBufferSize;
				if(position+searchReadBufferSize>mbb.limit()){
					l=(int)(mbb.limit()-position);
				}
				mbb.position((int) (position%Integer.MAX_VALUE));
				mbb.get(bytes,0,l);
				return revers(bytes);
			}else {
				return -2;
			}
		}else{
			MappedByteBuffer mbb=getMBB(filePath, fname, position);
			if(mbb!=null){
				int l=searchReadBufferSize;
				if(position+searchReadBufferSize>mbb.limit()){
					l=(int)(mbb.limit()-position);
				}
				mbb.position((int) (position%Integer.MAX_VALUE));
				mbb.get(bytes,0,l);
			}else {
				return -2;
			}
		}
		return -1;
	}
	protected MappedByteBuffer getMBB(String filePath,String fname,long position){
		RandomAccessFile raf = null;
		MappedByteBuffer mbb = null;
		String fpath=StringUtil.getHashPathFile(filePath,fname,false);
		int p=(int) (position/Integer.MAX_VALUE);
		long key=((long)fpath.hashCode()<<32)|p;
		try{
			if(fileMBB.containsKey(key)){
				mbb=fileMBB.get(key);
			}else{
				raf=new RandomAccessFile(fpath, "r");
				FileChannel fc=raf.getChannel();
				long fsize=fc.size();
				long pmax=p*Integer.MAX_VALUE;
				if(pmax+Integer.MAX_VALUE>fsize){
					mbb=fc.map(FileChannel.MapMode.READ_ONLY, pmax, fsize-pmax);
				}else{
					mbb=fc.map(FileChannel.MapMode.READ_ONLY, pmax, Integer.MAX_VALUE);
				}
				fileMBB.put(key, mbb);
			}
		}catch (Exception e) {fileMBB.put(key, null);}
		return mbb;
	}
	protected short aANDb(byte[]a,byte[]b){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			if(a[i]==0){
				x0++;
			}else if(a[i]==-1){
				a[i]=b[i];
				x1++;
			}else if(a[i]!=b[i]){
				a[i]&=b[i];
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
	protected short aORb(byte[]a,byte[]b){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			if(a[i]==0){
				a[i]=b[i];
				x0++;
			}else if(a[i]==-1){
				x1++;
			}else if(a[i]!=b[i]){
				a[i]|=b[i];
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
	protected short revers(byte[]a){
		int x0=0;
		int x1=0;
		for(int i=0;i<searchReadBufferSize;i++){
			a[i]=(byte) ~a[i];
			if(a[i]==0){
				x0++;
			}else if(a[i]==-1){
				x1++;
			}
		}
		if(x0==searchReadBufferSize){
			return 0;
		}else if(x1==searchReadBufferSize){
			return 1;
		}else {
			return -1;
		}
	}
}
