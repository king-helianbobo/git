package org.soul.elasticSearch.plugin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.lionsoul.jcseg.JcSegment;
import org.lionsoul.jcseg.pinyin.ChineseHelper;
import org.soul.splitWord.BasicAnalysis;
import org.soul.utility.JcsegInstance;

public class PinyinTokenFilter extends TokenFilter {

	private static Log log = LogFactory.getLog(PinyinTokenFilter.class);

	private char[] curTermBuffer;
	private int curTermLength;
	private int curNumber = 0; // 0 is Chinese term,1 is Chinese pinyin
	private int totalNumber = 3; // 需要转换几次，默认是3次
	private boolean needPinyinConv = true;
	private boolean needSynonymConv = true;
	private int tokenStart;
	private static JcSegment seg = JcsegInstance.instance();
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	public static final String TYPE_SYNONYM = "SYNONYM";

	public PinyinTokenFilter(TokenStream input) { // accept one token stream
		super(input);
	}

	/*
	 * this will return Chinese word ant its corresponding pinyin
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (curTermBuffer == null) {
				if (!input.incrementToken()) { // 如果流中已经没Token
					return false;
				} else {
					curTermLength = termAtt.length();
					curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					needPinyinConv = true;
					needSynonymConv = true;
					totalNumber = 3;
					if (!ChineseHelper.containChineseChar(new String(
							curTermBuffer))) {
						// 如果不包含汉字，不需要转换拼音
						needPinyinConv = false;
						totalNumber--;
					}
					tokenStart = offsetAtt.startOffset();
					curNumber = 0; // first time we get equivalent Term
				}
			}
			if (curNumber < totalNumber) {
				if (curNumber == 0) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenStart + curTermLength);
					termAtt.copyBuffer(curTermBuffer, 0, curTermLength);
				} else if (needPinyinConv) {
					String text = new String(curTermBuffer);
					Reader reader = new StringReader(text);
					String pinyin = seg.convertToPinyin(reader);
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenStart + curTermLength);
					// log.info(text + "," + pinyin);
					termAtt.copyBuffer(pinyin.toCharArray(), 0, pinyin.length());
					typeAtt.setType("PINYIN");
					// curTermBuffer = null;
					needPinyinConv = false;
				} else if (needSynonymConv) {
					String text = new String(curTermBuffer);
					String synonym = "[长度" + String.valueOf(text.length())
							+ text + "]";
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenStart + curTermLength);
					termAtt.copyBuffer(synonym.toCharArray(), 0,
							synonym.length());
					typeAtt.setType("SYNONYM");
					// curTermBuffer = null;
					needSynonymConv = false;
				}
				curNumber++;
				return true;
			} else
				curTermBuffer = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		curTermBuffer = null;
	}
}
