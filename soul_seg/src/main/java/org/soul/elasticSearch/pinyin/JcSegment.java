package org.soul.elasticSearch.pinyin;

import java.io.*;

import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.DictionaryFactory;
import org.lionsoul.jcseg.core.ISegment;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.core.SegmentFactory;
import org.lionsoul.jcseg.util.IStringBuffer;

public class JcSegment {
	private ISegment seg = null;

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
	public String segment(String str) throws IOException {
		StringBuffer sb = new StringBuffer();
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
			word = null;
		}
		return sb.toString();
	}

	public String segment(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		IStringBuffer isb = new IStringBuffer();
		IWord word = null;
		boolean isFirst = true;
		seg.reset(reader);
		while ((word = seg.next()) != null) {
			if (isFirst) {
				// sb.append("[" + word.getValue() + "/" + word.getPinyin() +
				// "]");
				sb.append(word.getPinyin());
				isFirst = false;
			} else {
				// sb.append(word.getPinyin());
				// sb.append(" ");
				// sb.append("[" + word.getValue() + "/" + word.getPinyin() +
				// "]");
				sb.append(word.getPinyin());
			}
			word = null;
		}
		String[] strs = sb.toString().split(" ");
		for (String str : strs) {
			// System.out.println("[" + str + "]");
			isb.append(str);
		}
		return isb.toString();
	}
}
