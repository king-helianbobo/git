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
import org.lionsoul.jcseg.JcSegment;
import org.soul.splitWord.BasicAnalysis;
import org.soul.utility.JcsegInstance;

public class PinyinTokenFilter extends TokenFilter {

	private static Log log = LogFactory.getLog(PinyinTokenFilter.class);

	private char[] curTermBuffer;
	private int curTermLength;
	private int curNumber = 0; // 0 is Chinese term,1 is Chinese pinyin
	private final int totalNumber = 2; // get two instances
	private int tokenStart;
	private static JcSegment seg = JcsegInstance.instance();
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

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
				if (!input.incrementToken()) { // 如果流中已经没Token了
					return false;
				} else {
					curTermLength = termAtt.length();
					curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					curNumber = 0; // first time we get equivalent Term
					tokenStart = offsetAtt.startOffset();
				}
			}
			if (curNumber < totalNumber) {
				if (curNumber == 0) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenStart + curTermLength);
					termAtt.copyBuffer(curTermBuffer, 0, curTermLength);
				} else if (curNumber == 1) {
					String text = new String(curTermBuffer);
					Reader reader = new StringReader(text);
					String pinyin = seg.convertToPinyin(reader);
					if (!pinyin.equals(text)) {// 有拼音的地方需要转换
						clearAttributes();
						offsetAtt.setOffset(tokenStart, tokenStart
								+ curTermLength);
						log.info(text + "," + pinyin);
						termAtt.copyBuffer(pinyin.toCharArray(), 0,
								pinyin.length());
					} else {// 没有拼音的地方直接忽略
						log.info(text + "," + pinyin);
						clearAttributes();
					}
					curTermBuffer = null;
				}
				curNumber++;
				return true;
			}
			curTermBuffer = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		curTermBuffer = null;
	}
}
