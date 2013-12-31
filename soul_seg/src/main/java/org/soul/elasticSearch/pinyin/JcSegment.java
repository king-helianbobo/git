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
import org.soul.test.spellCheck;

public class JcSegment {
	private ISegment seg = null;
	private static Log log = LogFactory.getLog(JcSegment.class);

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
		return convertToPinyin(reader);
	}

	public String convertToPinyin(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		IStringBuffer isb = new IStringBuffer();
		IWord word = null;
		boolean isFirst = true;
		seg.reset(reader);
		while ((word = seg.next()) != null) {
			String pinyin = word.getPinyin();
			// log.info("pinyin= " + pinyin + ",word = " + word.getValue());
			if ((pinyin == null) || (pinyin.equals("null"))) {
				// add another check ,assure pinyin is not null
				pinyin = PinyinHelper.convertToPinyinString(word.getValue(),
						"", PinyinFormat.WITHOUT_TONE);
			}
			if (isFirst) {
				sb.append(pinyin);
				isFirst = false;
			} else {
				sb.append(pinyin);
			}
			word = null;
		}
		String[] strs = sb.toString().split(" ");
		for (String str : strs) {
			isb.append(str);
		}
		return isb.toString();
	}
}
