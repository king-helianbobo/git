package org.soul.elasticSearch.pinyin;

import java.io.*;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.DictionaryFactory;
import org.lionsoul.jcseg.core.ISegment;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.core.SegmentFactory;
import org.lionsoul.jcseg.util.IStringBuffer;

public class JcSegment {

	private static Log log = LogFactory.getLog(JcSegment.class);
	private ISegment seg = null;
	// private static final ISegment seg = (ISegment) new JcSegment();

	public JcSegment() {
		JcsegTaskConfig config = new JcsegTaskConfig();
		ADictionary dic = DictionaryFactory.createDefaultDictionary(config);
		try {
			seg = SegmentFactory.createJcseg(JcsegTaskConfig.COMPLEX_MODE,
					new Object[]{config, dic});
		} catch (JcsegException e) {
			e.printStackTrace();
		}

	}

	// turn Chinese chars to pinyin
	public String convertToPinyin(String text) throws IOException {
		Reader reader = new StringReader(text);
		return convertToPinyin(reader);
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
	public String convertToPinyin(Reader reader) throws IOException {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			IStringBuffer isb = new IStringBuffer();
			IWord word = null;
			seg.reset(reader);
			while ((word = seg.next()) != null) {
				String pinyin = word.getPinyin();
				String origWord = word.getValue();
				if (origWord.length() == 1) {
					pinyin = PinyinHelper.convertToPinyinArray(
							origWord.charAt(0), PinyinFormat.WITHOUT_TONE)[0];
				}
				if ((pinyin == null) || (pinyin.equals("null"))) {
					// add another check ,assure pinyin String not null
					pinyin = PinyinHelper.convertToPinyinString(
							word.getValue(), "", PinyinFormat.WITHOUT_TONE);
				}
				log.info("拼音 = " + pinyin + ", 分词 = " + origWord);
				sb.append(pinyin);
				word = null;
			}
			String[] strs = sb.toString().split(" ");
			for (String str : strs) {
				if (!str.equals(""))
					isb.append(str);
			}
			return isb.toString();
		}
	}
}
