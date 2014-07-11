package com.splitword.lionsoul.jcseg;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.splitword.lionsoul.jcseg.core.ADictionary;
import com.splitword.lionsoul.jcseg.core.DictionaryFactory;
import com.splitword.lionsoul.jcseg.core.ISegment;
import com.splitword.lionsoul.jcseg.core.IWord;
import com.splitword.lionsoul.jcseg.core.JcsegException;
import com.splitword.lionsoul.jcseg.core.JcsegTaskConfig;
import com.splitword.lionsoul.jcseg.core.SegmentFactory;
import com.splitword.lionsoul.jcseg.util.IStringBuffer;
import com.splitword.lionsoul.jcseg.util.PinyinFormat;
import com.splitword.lionsoul.jcseg.util.PinyinHelper;

public class JcSegment {

	private static Log log = LogFactory.getLog(JcSegment.class);
	private ISegment seg = null;

	// private static final ISegment seg = (ISegment) new JcSegment();

	public JcSegment() {
		JcsegTaskConfig config = new JcsegTaskConfig();
		ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
		try {
			seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE,
					new Object[] { config, dic });
		} catch (JcsegException e) {
			e.printStackTrace();
		}

	}

	// turn Chinese chars to pinyin
	public String convertToPinyin(String text) throws IOException {
		Reader reader = new StringReader(text);
		return convertToPinyin(reader, text, false);
	}

	public String convertToPinyin(Reader reader) throws IOException {
		return convertToPinyin(reader, null, false);
	}

	public String convertToPinyin(Reader reader, boolean bSeperate)
			throws IOException {
		return convertToPinyin(reader, null, bSeperate);
	}

	/**
	 * convert Chinese characters to pinyin
	 * 
	 * @author LiuBo
	 * @since 2014-1-2
	 * @param reader
	 * @return
	 * @throws IOException
	 *             String
	 */
	private String convertToPinyin(Reader reader, String text, boolean bSeperate)
			throws IOException {
		synchronized (this) { // 同步一次，确保不同线程彼此隔离
			StringBuffer sb = new StringBuffer();
			IStringBuffer isb = new IStringBuffer();
			IWord word = null;
			seg.reset(reader);
			int i = 1;
			String sepTag = "";
			while ((word = seg.next()) != null) {
				String pinyin = word.getPinyin();
				String originalTerm = word.getValue();
				// log.info("第" + i + "个词 = " + originalTerm + ", 拼音 = " +
				// pinyin);
				if (originalTerm.length() == 1) {
					// 取多音字的最常见拼音
					pinyin = PinyinHelper.convertToPinyinString(originalTerm,
							sepTag, PinyinFormat.WITHOUT_TONE);
				}
				if ((pinyin == null) || (pinyin.equals("null"))) {
					// 增加一次检查，确保拼音不空
					pinyin = PinyinHelper.convertToPinyinString(originalTerm,
							sepTag, PinyinFormat.WITHOUT_TONE);
				}
				if (i == 1)
					sb.append(pinyin);
				else
					sb.append(" " + pinyin);
				word = null;
				i++;
			}
			if (bSeperate) {
				String str = sb.toString();
				str = str.replaceAll("u:", "v"); // 将拼音中的ü替换为v
				return str;
			} else {
				String[] strs = sb.toString().split(" ");
				for (String str : strs) {
					str = str.replaceAll("u:", "v"); // 将拼音中的ü替换为v
					if (!str.equals(""))
						isb.append(str);
				}
				// log.info(text + " ,拼音= " + sb.toString() + "[" +
				// isb.toString()
				// + "]");
				return isb.toString();
			}
		}
	}
}
