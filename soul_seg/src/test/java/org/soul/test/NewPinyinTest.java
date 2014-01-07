package org.soul.test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.Strings;
import org.soul.elasticSearch.pinyin.PinyinAnalyzer;
import org.soul.elasticSearch.pinyin.PinyinTokenFilter;
import org.soul.elasticSearch.pinyin.SoulEdgeNGramTokenFilter;
import org.soul.elasticSearch.plugin.SoulIndexAnalyzer;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

public class NewPinyinTest {

	private Log log = LogFactory.getLog(NewPinyinTest.class);

	String[] stopWords = {"and", "of", "the", "to", "is", "their", "can",
			"all", "i", "in"};
	String text = "沈从文 厦门 长春 长大";
	Reader reader = new StringReader(text);
	TokenStream result = null;

	@Test
	public void getPinYinTest() {
		String src = "沈从文";
		StringBuilder pinyinBuf = new StringBuilder();
		PinyinFormat outputFormat = PinyinFormat.WITHOUT_TONE;
		try {
			for (int i = 0; i < src.length(); i++) {
				String[] pinYins = PinyinHelper.convertToPinyinArray(
						src.charAt(i), outputFormat);

				if (pinYins != null && pinYins.length > 0) {// Chinese words
					for (int j = 0; j < pinYins.length; j++)
						pinyinBuf.append(pinYins[j] + " ");
					// pinyinBuf.append(pinYins[0] + " ");
				} else {// not Chinese words
					pinyinBuf.append(src.charAt(i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info(pinyinBuf.toString());
	}

	@Test
	public void TestSoulIndexAnalyzer() {
		Analyzer analyzer = new SoulIndexAnalyzer();
		String text4 = "沈从文 兴业银行 中华人民共和国";
		analyze(analyzer, text4);
	}

	@Test
	public void TestPinyinAnalyzer() {
		Analyzer analyzer = new PinyinAnalyzer();
		String text4 = "沈从文 兴业银行 中华人民共和国";
		analyze(analyzer, text4);
	}
	@Test
	public void TestAnalyzer() {
		result = new WhitespaceTokenizer(Version.LUCENE_CURRENT, reader);
		result = new PinyinTokenFilter(result);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.FRONT, 1, 20);
		OffsetAttribute offsetAttribute = result
				.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = result
				.addAttribute(CharTermAttribute.class);
		try {
			result.reset();
			while (result.incrementToken()) {
				int startOffset = offsetAttribute.startOffset();
				int endOffset = offsetAttribute.endOffset();
				String term = charTermAttribute.toString();
				log.info("\"" + term + "\"" + " start = " + startOffset
						+ ",end = " + endOffset);
			}
			result.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@BeforeMethod
	public void beforeMethod() {
	}

	@AfterMethod
	public void afterMethod() {
	}

	@BeforeClass
	public void beforeClass() {

	}

	@AfterClass
	public void afterClass() {
	}

	void analyze(Analyzer analyzer, String text) {
		try {
			// log.info("分词器:" + analyzer.getClass());
			// log.info(Strings.toUnderscoreCase("providerString"));
			// log.info(Strings.toCamelCase("_provider_string"));
			TokenStream tokenStream = analyzer.tokenStream("content",
					new StringReader(text));
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				CharTermAttribute charAttribute = tokenStream
						.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttribute = tokenStream
						.getAttribute(OffsetAttribute.class);
				log.info("[" + offsetAttribute.startOffset() + ","
						+ offsetAttribute.endOffset() + ","
						+ charAttribute.toString() + "]");
			}
			tokenStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
