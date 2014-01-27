package org.lionsoul.jcseg.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.DictionaryFactory;
import org.lionsoul.jcseg.core.ISegment;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.core.SegmentFactory;

public class JcsegTest {

	ISegment seg = null;
	private final Log log = LogFactory.getLog(JcsegTest.class);

	@Before
	public void startJcsegTest() throws JcsegException, IOException {
		JcsegTaskConfig config = new JcsegTaskConfig();
		ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
		seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE,
				new Object[]{config, dic});
		log.info("jcseg参数设置：");
		log.info("最大切分匹配词数：" + config.MAX_LENGTH);
		log.info("最大混合中文长度：" + config.MIX_CN_LENGTH);
		log.info("开启中文人名识别：" + config.I_CN_NAME);
		log.info("最大姓氏前缀修饰：" + config.MAX_CN_LNADRON);
		log.info("最大标点配对词长：" + config.PPT_MAX_LENGTH);
		log.info("词库词条拼音加载：" + config.LOAD_CJK_PINYIN);
		log.info("分词词条拼音追加：" + config.APPEND_CJK_PINYIN);
		log.info("词库同义词的载入：" + config.LOAD_CJK_SYN);
		log.info("分词同义词的追加：" + config.APPEND_CJK_SYN);
		log.info("词库词条词性载入：" + config.LOAD_CJK_POS);
		log.info("去除切分后噪音词：" + config.CLEAR_STOPWORD);
		log.info("中文数字转阿拉伯：" + config.CNNUM_TO_ARABIC);
		log.info("中文分数转阿拉伯：" + config.CNFRA_TO_ARABIC);
		log.info("保留未识别的字符：" + config.KEEP_UNREG_WORDS);
		log.info("英文词条二次切分：" + config.EN_SECOND_SEG);
		log.info("姓名成词歧义阕值：" + config.NAME_SINGLE_THRESHOLD + "\n");
	}

	private void segment(String str) throws IOException {
		StringBuffer sb = new StringBuffer();
		// seg.setLastRule(null);
		IWord word = null;
		boolean isFirst = true;
		seg.reset(new StringReader(str));
		while ((word = seg.next()) != null) {
			if (isFirst) {
				sb.append("[" + word.getValue() + "," + word.getPinyin() + "]");
				isFirst = false;
			} else {
				sb.append(" ");
				sb.append("[" + word.getValue() + "," + word.getPinyin() + "]");
			}
			// append the part of the speech
			if (word.getPartSpeech() != null) {
				sb.append('/');
				sb.append(word.getPartSpeech()[0]);
			}
			// clear the allocations of the word.
			word = null;
		}
		log.info("分词结果：" + sb.toString());
	}
	@Test
	public void segmentWordTest() throws JcsegException, IOException {
		String str = "关羽字云长河东解县人，福建新野银行";
		log.info(str);
		try {
			segment(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
