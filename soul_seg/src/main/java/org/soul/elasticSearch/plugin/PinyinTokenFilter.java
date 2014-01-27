package org.soul.elasticSearch.plugin;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.lionsoul.jcseg.JcSegment;
import org.lionsoul.jcseg.pinyin.ChineseHelper;
import org.soul.utility.JcsegInstance;

public class PinyinTokenFilter extends TokenFilter {

	private static Log log = LogFactory.getLog(PinyinTokenFilter.class);
	private int totalNumber = 1; // number of to be convert
	// private boolean needPinyinConv = false;
	// private boolean needSynonymConv = false;
	private int tokenStart;
	private int curNumber = 0; // 0 is Chinese term,1 is Chinese pinyin
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
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
					totalNumber = 1;
					if (ChineseHelper.containChineseChar(originalToken)) {
						// if not consist of Chinese chars, no need to get
						// Pinyin
						Reader reader = new StringReader(originalToken);
						pinyin = seg.convertToPinyin(reader);
						totalNumber += 1;
						log.info("text =  " + originalToken + ", pinyin = "
								+ pinyin);
					} else {
						pinyin = null;
					}
					if (synonymTree != null) {
						List<String> list = synonymTree.get(originalToken);
						if (list != null) {
							log.info("originalToken is " + originalToken);
							// needSynonymConv = true;
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
					curNumber = 0; // first time we get equivalent Term
				}
			}
			if (curNumber < totalNumber) {
				if (curNumber == 0) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart,
							tokenStart + originalToken.length());
					termAtt.append(originalToken);
					if (pinyin == null)
						typeAtt.setType(TYPE_WORD);
					else
						typeAtt.setType(TYPE_HANZI);
				} else if (pinyin != null) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart,
							tokenStart + originalToken.length());
					// log.info(text + "," + pinyin);
					termAtt.copyBuffer(pinyin.toCharArray(), 0, pinyin.length());
					typeAtt.setType(TYPE_PINYIN);
					pinyin = null;
				} else if (synonymList != null) {
					clearAttributes();
					offsetAtt.setOffset(tokenStart,
							tokenStart + originalToken.length());
					String text = synonymList.remove(0);
					termAtt.append(text);
					typeAtt.setType(TYPE_SYNONYM);
					if (synonymList.size() == 0)
						synonymList = null;
					// needSynonymConv = false;
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
