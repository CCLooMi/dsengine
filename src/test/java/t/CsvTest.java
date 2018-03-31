package t;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**© 2015-2018 Chenxj Copyright
 * 类    名：CsvTest
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月28日-下午9:05:05
 */
public class CsvTest {
	public static void main(String[] args) throws Exception {
		File dir=Paths.get(System.getProperty("user.dir"), "csv").toFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		CSVFormat format=CSVFormat.DEFAULT
				.withHeader("id","name","info");
		File csv=Paths.get(dir.getAbsolutePath(),"t.csv").toFile();
		
		//写入文件
		FileWriter fw=new FileWriter(csv);
		CSVPrinter cprint=new CSVPrinter(fw, format);
		cprint.printRecord("12","Seemie","寻\n秦\n记");
		cprint.close();
		
		//读取文件 
		format=format.withSkipHeaderRecord();//跳过头
		Reader in=new FileReader(csv);
		Iterable<CSVRecord>records=format.parse(in);
		for(CSVRecord record:records) {
			System.out.print(record.get("id"));
			System.out.print(',');
			System.out.print(record.get("name"));
			System.out.print(',');
			System.out.print(record.get("info"));
		}
		
	}
}
