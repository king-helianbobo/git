package org.soul.test;

import java.io.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.Strings;
import org.soul.elasticSearch.pinyin.PinyinTokenFilter;
import org.soul.elasticSearch.pinyin.SoulEdgeNGramTokenFilter;
import org.soul.elasticSearch.pinyin.PinyinAnalyzer;

public class PinyinTest {
	// public static String getPinYin(String src) {
	// StringBuilder pinyinBuf = new StringBuilder();
	// HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
	// outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	// outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	// outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	// try {
	// for (int i = 0; i < src.length(); i++) {
	// String[] pinYins = PinyinHelper.toHanyuPinyinStringArray(
	// src.charAt(i), outputFormat);
	//
	// if (pinYins != null && pinYins.length > 0) {// 汉语
	// for (int j = 0; j < pinYins.length; j++)
	// pinyinBuf.append(pinYins[j] + " ");
	// // pinyinBuf.append(pinYins[0] + " ");
	// } else {// 非汉语
	// pinyinBuf.append(src.charAt(i));
	// }
	// }
	// } catch (BadHanyuPinyinOutputFormatCombination e) {
	// e.printStackTrace();
	// }
	// return pinyinBuf.toString();
	// }

	public static void analyze(Analyzer analyzer, String text) {
		try {
			System.out.println("分词器:" + analyzer.getClass());
			System.out.println(Strings.toUnderscoreCase("providerString"));

			System.out.println(Strings.toCamelCase("_provider_string"));
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

	public static void noMean() {

		String[] stopWords = {"and", "of", "the", "to", "is", "their", "can",
				"all", "i", "in"};
		String text = "沈从文 厦门 长春 长大";
		Reader reader = new StringReader(text);
		TokenStream result = new WhitespaceTokenizer(Version.LUCENE_46, reader);
		// TokenFilter lowerCaseFilter = new LowerCaseFilter(Version.LUCENE_46,
		// tokenizer);

		// TokenStream result = new LowerCaseFilter(Version.LUCENE_46,
		// tokenizer);

		result = new PinyinTokenFilter(result);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.FRONT, 1, 20);
		// result = new SoulEdgeNGramTokenFilter(result,
		// SoulEdgeNGramTokenFilter.Side.BACK, 2, 20);
		// result = new
		// org.apache.lucene.analysis.icu.ICUTransformFilter(result,
		// NAME_PINYIN_TRANSLITERATOR);
		// result = new ICUTransformFilter(result, NAME_PINYIN_TRANSLITERATOR);
		// return result;
		//
		// TokenStream tokenStream = lowerCaseFilter;

		OffsetAttribute offsetAttribute = result
				.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = result
				.addAttribute(CharTermAttribute.class);
		// Transliterator NAME_PINYIN_TRANSLITERATOR = Transliterator
		// .getInstance("Han-Latin;NFD;[[:NonspacingMark:][:Space:]] Remove");
		// result = new ICUTransformFilter(result, NAME_PINYIN_TRANSLITERATOR);
		// // Transliterator literator2 = Transliterator
		// // .createFromRules(
		// // null,
		// //
		// ":: Han-Latin/Names;[[:space:]][bpmfdtnlgkhjqxzcsryw] { [[:any:]-[:white_space:]] >;::NFD;[[:NonspacingMark:][:Space:]]>;",
		// // Transliterator.FORWARD);
		//
		// result = new
		// org.apache.lucene.analysis.icu.ICUTransformFilter(result,
		// NAME_PINYIN_TRANSLITERATOR);

		// Transliterator[] literators = {NAME_PINYIN_TRANSLITERATOR,
		// literator2};
		// result = new SoulICUTransformFilter(result, literators);
		try {
			result.reset();
			while (result.incrementToken()) {
				int startOffset = offsetAttribute.startOffset();
				int endOffset = offsetAttribute.endOffset();
				String term = charTermAttribute.toString();
				System.out.println("\"" + term + "\"" + " start = "
						+ startOffset + ",end = " + endOffset);
			}
			result.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// WhitespaceTokenizer(Version.LUCENE_31, new StringReader(text));
		//
		// TokenStream tokenStream = analyzer.tokenStream("content",
		// new StringReader(text));
		// TokenStream result = new LowerCaseFilter(Version.LUCENE_31, stream);
		// result = new PimEdgeNGramTokenFilter(result,
		// PimEdgeNGramTokenFilter.Side.BACK, 2, 20);
		// result = new PimEdgeNGramTokenFilter(result,
		// PimEdgeNGramTokenFilter.Side.FRONT, 2, 20);

		// return result;
	}

	public static void main(String[] args) {
		// System.out.println(getPinYin("Hello,欢迎来到长春,厦门,红色中国"));

		Analyzer analyzer = new PinyinAnalyzer();
		// Analyzer analyzer = new SoulIndexAnalyzer();
		String enText = "No news is good news";
		String chText = "没消息就是好消息";
		String text3 = "Hello,欢迎来到长春，厦门，重庆，红色中国！";
		String text4 = "沈从文 兴业银行 中华人民共和国";

		analyze(analyzer, text4);
		// noMean();
		// analyze(analyzer, chText);
		// analyze(analyzer, text3);
	}
}
