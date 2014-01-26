package org.lionsoul.jcseg.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.DictionaryFactory;
import org.lionsoul.jcseg.core.ISegment;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.core.SegmentFactory;

public class JcsegTest {

	ISegment seg = null;

	public JcsegTest() throws JcsegException, IOException {

		JcsegTaskConfig config = new JcsegTaskConfig();
		ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
		seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE,
				new Object[]{config, dic});

		// append pinyin
		// config.setAppendCJKPinyin(true);
		System.out.println("jcseg参数设置：");
		System.out.println("最大切分匹配词数：" + config.MAX_LENGTH);
		System.out.println("最大混合中文长度：" + config.MIX_CN_LENGTH);
		System.out.println("开启中文人名识别：" + config.I_CN_NAME);
		System.out.println("最大姓氏前缀修饰：" + config.MAX_CN_LNADRON);
		System.out.println("最大标点配对词长：" + config.PPT_MAX_LENGTH);
		System.out.println("词库词条拼音加载：" + config.LOAD_CJK_PINYIN);
		System.out.println("分词词条拼音追加：" + config.APPEND_CJK_PINYIN);
		System.out.println("词库同义词的载入：" + config.LOAD_CJK_SYN);
		System.out.println("分词同义词的追加：" + config.APPEND_CJK_SYN);
		System.out.println("词库词条词性载入：" + config.LOAD_CJK_POS);
		System.out.println("去除切分后噪音词：" + config.CLEAR_STOPWORD);
		System.out.println("中文数字转阿拉伯：" + config.CNNUM_TO_ARABIC);
		System.out.println("中文分数转阿拉伯：" + config.CNFRA_TO_ARABIC);
		System.out.println("保留未识别的字符：" + config.KEEP_UNREG_WORDS);
		System.out.println("英文词条二次切分：" + config.EN_SECOND_SEG);
		System.out.println("姓名成词歧义阕值：" + config.NAME_SINGLE_THRESHOLD + "\n");
	}

	public void segment(String str) throws IOException {

		StringBuffer sb = new StringBuffer();
		// seg.setLastRule(null);
		IWord word = null;
		long _start = System.nanoTime();
		boolean isFirst = true;
		int counter = 0;
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
			counter++;
		}
		long e = System.nanoTime();
		System.out.println("分词结果：");
		System.out.println(sb.toString());
		System.out.format("Done, total:" + seg.getStreamPosition() + ", split:"
				+ +counter + ", cost: %.5fsec(less than)\n",
				((float) e - _start) / 1E9);
	}

	/**
	 * @param args
	 * @throws JcsegException
	 * @throws IOException
	 */
	public static void main(String[] args) throws JcsegException, IOException {
		String str = "关羽字云长，女红";
		String cmd = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		JcsegTest demo = new JcsegTest();
		System.out.println(str);
		try {
			demo.segment(str);
			System.out.println("jcseg chinese word segment demo----+");
			System.out.println("|- Run quit or exit to exit.   |");
			System.out.println("---------------------------------+");
			do {
				System.out.print("jcseg>> ");
				cmd = reader.readLine();
				if (cmd == null)
					break;
				cmd = cmd.trim();
				if ("".equals(cmd))
					continue;
				if (cmd.equals("quit") || cmd.equals("exit")) {
					System.out.println("Thanks for trying jcseg, Bye!");
					System.exit(0);
				}
				demo.segment(cmd);
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
