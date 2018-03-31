package t;

import java.util.Arrays;

import com.ccloomi.dsengine.analyze.DefaultAnalyze;
import com.ccloomi.dsengine.analyze.IndexAnalyze;

/**© 2015-2018 Chenxj Copyright
 * 类    名：CollectTest
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2018年3月31日-下午7:59:48
 */
public class CollectTest {
	public static void main(String[] args) {
		IndexAnalyze analyze=new DefaultAnalyze();
		String[][]diff=analyze.difference("aabbcc", "aaccdd");
		System.out.println(Arrays.toString(diff[0]));
		System.out.println(Arrays.toString(diff[1]));
	}
}
