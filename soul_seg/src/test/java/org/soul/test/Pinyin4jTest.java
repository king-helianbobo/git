package org.soul.test;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.soul.elasticSearch.pinyin.PinyinAnalyzer;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin4jTest {
	public static String getPinYin(String src) {
		StringBuilder pinyinBuf = new StringBuilder();
		HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
		outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

		try {
			for (int i = 0; i < src.length(); i++) {
				String[] pinYins = PinyinHelper.toHanyuPinyinStringArray(
						src.charAt(i), outputFormat);

				if (pinYins != null && pinYins.length > 0) {// 汉语
					for (int j = 0; j < pinYins.length; j++)
						pinyinBuf.append(pinYins[j] + " ");
					// pinyinBuf.append(pinYins[0] + " ");
				} else {// 非汉语
					pinyinBuf.append(src.charAt(i));
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return pinyinBuf.toString();
	}

	public static void analyze(Analyzer analyzer, String text) {
		try {
			System.out.println("分词器:" + analyzer.getClass());
			TokenStream tokenStream = analyzer.tokenStream("content",
					new StringReader(text));
			tokenStream.reset();

			while (tokenStream.incrementToken()) {
				CharTermAttribute charAttribute = tokenStream
						.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttribute = tokenStream
						.getAttribute(OffsetAttribute.class);
				System.out.println("[" + offsetAttribute.startOffset() + ","
						+ offsetAttribute.endOffset() + ","
						+ charAttribute.toString() + "]");
			}
			tokenStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// System.out.println(getPinYin("Hello,欢迎来到长春,厦门,红色中国"));

		Analyzer analyzer = new PinyinAnalyzer();
		// Analyzer analyzer = new SoulIndexAnalyzer();
		String enText = "No news is good news";
		String chText = "没消息就是好消息";
		String text3 = "Hello,欢迎来到长春，厦门，重庆，红色中国！";

		analyze(analyzer, enText);
		analyze(analyzer, chText);
		analyze(analyzer, text3);
	}
}
