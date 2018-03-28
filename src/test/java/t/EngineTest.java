package t;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.ccloomi.dsengine.DSEngine;
import com.ccloomi.dsengine.Schema;
import com.ccloomi.dsengine.analyze.DefaultAnalyze;
import com.ccloomi.dsengine.bean.ResultBean;
import com.ccloomi.dsengine.field.IdField;
import com.ccloomi.dsengine.field.StringField;
import com.ccloomi.dsengine.query.QueryParser;
import com.ccloomi.dsengine.util.UUID;

/**© 2015-2018 Chenxj Copyright
 * 类    名：EngineTest
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月26日-下午4:00:55
 */
public class EngineTest {
	public static void main(String[] args) {
		String dpath=Paths.get(System.getProperty("user.dir"),"data").toString();
		
		Schema schema=new Schema("t_info");
		schema.addField(new IdField("id"));
		schema.addField(new StringField("info", -1, new DefaultAnalyze()));
		DSEngine engine=new DSEngine(dpath);
		engine.setSchema(schema);
		
//		String[]docs=new String[] {"河东狮吼河东狮吼","东西方不败","东成西就","言叶之庭日本","蜀山传张柏芝"};
//		for(int i=0;i<docs.length;i++) {
//			Map<String, Object>doc=new HashMap<>();
//			doc.put("id", UUID.randomUUID());
//			doc.put("info", docs[i]);
//			engine.addDocuments(doc);
//		}
//		engine.flush2disk();
		Scanner sc=new Scanner(System.in);
		while(true) {
			String queryStr=sc.nextLine();
			if(!"exit".equalsIgnoreCase(queryStr)){
				if(!"".equals(queryStr)){
					ResultBean result=engine.doQuery("t_info", QueryParser.parser(schema, queryStr));
					System.out.println(result);
				}else{
					ResultBean result=engine.doQuery("t_info", QueryParser.parser(schema, "{info:'东 蜀山'}"));
					System.out.println(result);
				}
			}else{
				break;
			}
		}
		sc.close();
	}
}
