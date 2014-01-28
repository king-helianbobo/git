package org.soul.elasticSearch.plugin;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class SoulEdgeNGramTokenFilter extends TokenFilter {
	public static final Side DEFAULT_SIDE = Side.FRONT;
	private static Log log = LogFactory.getLog(SoulEdgeNGramTokenFilter.class);
	public static int offset = 0;
	public static boolean isFront = true;
	private String originalText = null;
	private String convertedText = null;
	/** Specifies which side of the input the n-gram should be generated from */
	public static enum Side {

		/** Get the n-gram from the front of the input */
		FRONT {
			@Override
			public String getLabel() {
				return "front";
			}
		},

		/** Get the n-gram from the end of the input */
		BACK {
			@Override
			public String getLabel() {
				return "back";
			}
		},
		/** Get the n-gram from the end of the input */
		TWOSIDE {

			@Override
			public String getLabel() {
				return "twoside";
			}
		};

		public abstract String getLabel();

		// Get the appropriate Side from a string
		public static Side getSide(String sideName) {
			if (FRONT.getLabel().equals(sideName)) {
				return FRONT;
			}
			if (BACK.getLabel().equals(sideName)) {
				return BACK;
			}
			if (TWOSIDE.getLabel().equals(sideName)) {
				return BACK;
			}
			return null;
		}
	}

	private final int minGram;
	private final int maxGram;
	private Side side;
	private String type;
	private int position;
	private int curGramSize;
	private int tokStart;
	private int tokenEnd;
	private int tokenLength;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final PositionIncrementAttribute posAtt = addAttribute(PositionIncrementAttribute.class);

	/**
	 * Creates EdgeNGramTokenFilter that can generate n-grams in the sizes of
	 * the given range
	 * 
	 * @param input
	 *            {@link TokenStream} holding the input to be tokenized
	 * @param side
	 *            the {@link Side} from which to chop off an n-gram
	 * @param minGram
	 *            the smallest n-gram to generate
	 * @param maxGram
	 *            the largest n-gram to generate
	 */
	public SoulEdgeNGramTokenFilter(TokenStream input, Side side, int minGram,
			int maxGram) {
		super(input);
		if (side == null) {
			throw new IllegalArgumentException(
					"sideLabel must be either front or back");
		}

		if (minGram < 1) {
			throw new IllegalArgumentException(
					"minGram must be greater than zero");
		}

		if (minGram > maxGram) {
			throw new IllegalArgumentException(
					"minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.side = side;
	}

	public SoulEdgeNGramTokenFilter(TokenStream input, Side side, int minGram) {
		super(input);
		if (side == null) {
			throw new IllegalArgumentException(
					"sideLabel must be either front or back");
		}
		if (minGram < 1) {
			throw new IllegalArgumentException(
					"minGram must be greater than zero");
		}
		this.minGram = minGram;
		this.maxGram = 1000 * 1000 * 100; // no limit
		this.side = side;
	}

	/**
	 * Creates EdgeNGramTokenFilter that can generate n-grams in the sizes of
	 * the given range
	 * 
	 * @param input
	 *            {@link TokenStream} holding the input to be tokenized
	 * @param sideLabel
	 *            the name of the {@link Side} from which to chop off an n-gram
	 * @param minGram
	 *            the smallest n-gram to generate
	 * @param maxGram
	 *            the largest n-gram to generate
	 */
	public SoulEdgeNGramTokenFilter(TokenStream input, String sideLabel,
			int minGram, int maxGram) {
		this(input, Side.getSide(sideLabel), minGram, maxGram);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		while (true) {
			if (originalText == null) {
				if (!input.incrementToken()) {
					return false;
				} else {
					int curTermLength = termAtt.length();
					char[] curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					tokenLength = curTermLength;
					originalText = new String(curTermBuffer);
					tokStart = offsetAtt.startOffset();
					tokenEnd = offsetAtt.endOffset();
					type = typeAtt.type();
					position = posAtt.getPositionIncrement();
					if (type.equalsIgnoreCase(PinyinTokenFilter.TYPE_HANZI)) {
						curGramSize = 1;
						// set minimum Chinese characters to 1
					} else if (type
							.equalsIgnoreCase(PinyinTokenFilter.TYPE_SYNONYM)) {
						curGramSize = curTermLength;
						// synonym word not convert to EdgeNGram format
					} else if (type
							.equalsIgnoreCase(PinyinTokenFilter.TYPE_PINYIN)) {
						curGramSize = minGram;
						convertedText = originalText.replaceAll(" ", "");
						tokenLength = convertedText.length();
					} else {
						// English words and Hanyu pinyin need keep minGram
						curGramSize = minGram;
					}
				}
			}
			if (curGramSize <= maxGram) { // current gram length
				if (curGramSize >= tokenLength) {
					// if remaining input is too short, still generate
					if (type.equals(PinyinTokenFilter.TYPE_PINYIN))
						pinyinOperation(0, tokenLength);
					else
						commonOperation(0, tokenLength);
					originalText = null;
					return true;
				} else {
					if (side == Side.FRONT || side == Side.BACK) {
						int start = side == Side.FRONT ? 0 : tokenLength
								- curGramSize;
						if (type.equals(PinyinTokenFilter.TYPE_PINYIN))
							pinyinOperation(start, curGramSize);

						else
							commonOperation(start, curGramSize);
						curGramSize++;
						return true;
					} else {
						int backStart = tokenLength - curGramSize;
						clearAttributes();
						if (isFront) {
							if (type.equals(PinyinTokenFilter.TYPE_PINYIN))
								pinyinOperation(0, curGramSize);

							else
								commonOperation(0, curGramSize);
							isFront = false;
						} else {
							if (type.equals(PinyinTokenFilter.TYPE_PINYIN))
								pinyinOperation(backStart, curGramSize);

							else
								commonOperation(backStart, curGramSize);
							isFront = true;
							curGramSize++;
						}
						return true;
					}
				}
			}
			originalText = null;
		}
	}
	private void commonOperation(int startOffset, int length) {
		clearAttributes();
		termAtt.append(originalText
				.substring(startOffset, startOffset + length));
		if (type.equalsIgnoreCase(PinyinTokenFilter.TYPE_SYNONYM))
			offsetAtt.setOffset(tokStart, tokenEnd);
		else
			offsetAtt.setOffset(tokStart, tokStart + length);
		// log.info(offsetAtt.startOffset() + "," + offsetAtt.endOffset());
		typeAtt.setType(type);
		posAtt.setPositionIncrement(position);
	}
	private void pinyinOperation(int startOffset, int length) {
		clearAttributes();
		String subPinyin = convertedText.substring(startOffset, startOffset
				+ length);
		int j = 0;
		int numSpace = 0;
		for (int i = 0; i < originalText.length(); i++) {
			if (j >= subPinyin.length())
				break;
			if (originalText.charAt(i) == subPinyin.charAt(j)) {
				j++;
				continue;
			} else if (originalText.charAt(i) == ' ') {
				numSpace++;
			} else {
				try {
					throw new Exception("Pinyin " + originalText
							+ " is unlegal!");
				} catch (Exception e) {
					log.error("Pinyin: " + originalText + " is unlegal!");
					e.printStackTrace();
				}
			}

		}
		if ((tokStart + numSpace + 1) > tokenEnd)
			log.error("tokenStart and tokenEnd error!");
		offsetAtt.setOffset(tokStart, tokStart + numSpace + 1);
		termAtt.append(subPinyin);
		typeAtt.setType(type);
		posAtt.setPositionIncrement(position);
	}
	@Override
	public void reset() throws IOException {
		super.reset();
		originalText = null;
	}
}