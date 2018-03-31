package com.ccloomi.dsengine.linkthread;

import java.io.Closeable;
import java.io.IOException;

import com.ccloomi.dsengine.DataAccess;
import com.ccloomi.dsengine.bean.MapBean;


/**@类名 SchemaReader
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年1月16日-下午1:47:16
 */
public class SchemaReader extends BaseLinkedThread<Long, MapBean> implements Closeable{
	private DataAccess da;
	public SchemaReader(DataAccess da) {
		this.setName("LinkThread_A-Schema Reader");
		this.da=da;
		this.setNextThread(new Score())
		.setNextThread(new Sort(da));
	}

	@Override
	public void close() throws IOException {
		// TODO 需要关闭所有打开的文件资源
	}

	@Override
	public void reset() {}

	@Override
	public MapBean processData(Long docId) {
		return da.readDocumentFieldDataInQuery(query,docId);
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get() {
		if(nextThread!=null) {
			return (T) da.repairDoc(nextThread.get());
		}
		return null;
	}
}
