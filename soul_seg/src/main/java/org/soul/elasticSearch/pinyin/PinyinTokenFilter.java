package org.soul.elasticSearch.pinyin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class PinyinTokenFilter extends TokenFilter {

	private char[] curTermBuffer;
	private int curTermLength;
	private int curNumber = 0; // 0 is Chinese term,1 is Chinese pinyin
	private final int totalNumber = 2; // we only want get two instances
	private int tokenStart;
	private static JcSegment seg = new JcSegment();
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
				if (!input.incrementToken()) { // if there are no token
					return false;
				} else {
					curTermLength = termAtt.length();
					curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					// curTermBuffer = termAtt.buffer().clone(); // clone buffer
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
					//char[] buffer = new char[curTermLenth];
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenStart + curTermLength);
					String text = new String(curTermBuffer);
					// String text1 = text.substring(0, curTermLength);
					// String text = new String(curTermBuffer);
					//System.out.println("haha ,text=[" + text + "]");
					Reader reader = new StringReader(text);
					String pinyin = seg.segment(reader);
					// String pinyin = "haha";
					//System.out.println("haha ,we get " + pinyin);
					termAtt.copyBuffer(pinyin.toCharArray(), 0, pinyin.length());
					// termAtt.append(pinyin);

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
