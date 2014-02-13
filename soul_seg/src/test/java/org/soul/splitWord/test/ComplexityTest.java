package org.soul.splitWord.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.soul.library.InitDictionary;
import org.soul.utility.WordAlter;

public class ComplexityTest {

	private final Log log = LogFactory.getLog(ComplexityTest.class);
	// @Ignore()
	@Test
	public void memoryTest() {
		long Mbyte = 1024 * 1024;
		log.info("最大可用内存" + (Runtime.getRuntime().maxMemory() / Mbyte));
		log.info("JVM空闲内存" + (Runtime.getRuntime().freeMemory() / Mbyte));
		log.info("JVM占用的内存总数" + (Runtime.getRuntime().totalMemory() / Mbyte));
		try {
			InitDictionary.initArrays();
			log.info("最大可用内存" + (Runtime.getRuntime().maxMemory() / Mbyte));
			log.info("JVM空闲内存" + (Runtime.getRuntime().freeMemory() / Mbyte));
			log.info("JVM占用的内存总数"
					+ (Runtime.getRuntime().totalMemory() / Mbyte));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Ignore
	@Test
	public void traditionalToSimplifiedChineseTest() {
		List<String> all = new ArrayList<String>();
		all.add("關注十八大：台港澳密集解讀十八大報告”");
		all.add("关注十八大：台港澳密集解读十八大报告”");
		all.add("參選國民黨主席？ 胡志強首度鬆口稱“會考慮”");
		all.add("参选国民党主席？ 胡志强首度松口称“会考虑”");
		all.add("駁謝長廷“國民黨像東廠” 藍營吁其勿惡意嘲諷");
		all.add("驳谢长廷“国民党像东厂” 蓝营吁其勿恶意嘲讽");
		all.add("台藝人陳俊生出軌逼死女友 絕情獸行遭曝光");
		all.add("台艺人陈俊生出轨逼死女友 绝情兽行遭曝光");
		all.add("林益世想回高雄探母 法官警告勿有逃亡念頭");
		all.add("林益世想回高雄探母 法官警告勿有逃亡念头");
		for (int i = 0; i < all.size(); i++) {
			if (i % 2 == 0) {
				String str1 = InitDictionary
						.TraditionalToSimplified(all.get(i));
				assertEquals(all.get(i + 1), str1);
			}
		}
	}

	// @Ignore()
	@Test
	public void wordAlterTest() {
		String str = "。，、”ｓｄｆｓｄｆ多啦哆啦Ａａ梦１";
		String result = WordAlter.alterAlphaAndNumber(str, 0, str.length());
		log.info(countChineseChars(str) + " :[ " + result + " ]");
	}

	private int countChineseChars(String s) {
		int count = 0;
		Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(s);
		while (matcher.find()) {
			count++;
		}
		return count;
	}
}
