package org.elasticsearch.plugin;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.splitword.lionsoul.jcseg.JcSegment;
import org.splitword.lionsoul.jcseg.pinyin.ChineseHelper;
import org.splitword.soul.utility.JcsegInstance;

public class PinyinTokenFilter extends TokenFilter {

	private static Log log = LogFactory.getLog(PinyinTokenFilter.class);
	private int totalNumber = 1; // number of to be convert
	private int tokenStart;
	private int tokenEnd;
	private int position = 1;
	private int curNumber = 0;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final PositionIncrementAttribute posAtt = addAttribute(PositionIncrementAttribute.class);
	public static final String TYPE_SYNONYM = "SOUL_SYNONYM";
	public static final String TYPE_PINYIN = "SOUL_PINYIN";
	public static final String TYPE_HANZI = "SOUL_HANZI";
	public static final String TYPE_WORD = "SOUL_WORD";
	private static JcSegment seg = JcsegInstance.instance();
	private TreeMap<String, List<String>> synonymTree;
	private List<String> synonymList = null;
	private String pinyin = null;
	private String originalToken = null;

	public PinyinTokenFilter(TokenStream input) { // accept one token stream
		super(input);
		synonymTree = null;
	}

	public PinyinTokenFilter(TokenStream input,
			TreeMap<String, List<String>> synonymTree) {
		// accept one token stream and synonym tree
		super(input);
		this.synonymTree = synonymTree;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (originalToken == null) {
				if (!input.incrementToken()) {// no more tokens
					return false;
				} else {
					int curTermLength = termAtt.length();
					char[] curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					originalToken = new String(curTermBuffer);
					position = posAtt.getPositionIncrement();
					// log.info("position = " + position);
					totalNumber = 1;
					if (ChineseHelper.containChineseChar(originalToken)) {
						// if not include Chinese chars, no need get Pinyin
						Reader reader = new StringReader(originalToken);
						pinyin = seg.convertToPinyin(reader, true);
						totalNumber += 1;
					} else {
						pinyin = null;
					}
					if (synonymTree != null) {
						List<String> list = synonymTree.get(originalToken);
						if (list != null) {
							totalNumber += (list.size());
							synonymList = new LinkedList<String>();
							synonymList.addAll(list);
						} else {
							synonymList = null;
						}
					} else {
						synonymList = null;
					}
					tokenStart = offsetAtt.startOffset();
					tokenEnd = offsetAtt.endOffset();
					curNumber = 0; // first time we get equivalent Term
				}
			}
			if (curNumber < totalNumber) {
				if (curNumber == 0) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenEnd);
					termAtt.append(originalToken);
					posAtt.setPositionIncrement(position);
					if (pinyin == null)
						typeAtt.setType(TYPE_WORD);
					else
						typeAtt.setType(TYPE_HANZI);
				} else if (pinyin != null) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenEnd);
					termAtt.copyBuffer(pinyin.toCharArray(), 0, pinyin.length());
					typeAtt.setType(TYPE_PINYIN);
					posAtt.setPositionIncrement(position);
					pinyin = null;
				} else if (synonymList != null) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart, tokenEnd);
					String text = synonymList.remove(0);
					termAtt.append(text);
					typeAtt.setType(TYPE_SYNONYM);
					posAtt.setPositionIncrement(position);
					// log.info("Synonym Token is [ " + text + " ]");
					if (synonymList.size() == 0)
						synonymList = null;
				}
				curNumber++;
				return true;
			} else
				originalToken = null;
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		originalToken = null;
	}
}
