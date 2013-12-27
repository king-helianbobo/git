package org.soul.elasticSearch.pinyin;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * 
 * This {@link org.apache.lucene.analysis.Tokenizer} is copied and modified from
 * {@link org.apache.lucene.analysis.EdgeNGramTokenFilter} to accept term with
 * length less then minimum-gram
 */
public class SoulEdgeNGramTokenFilter extends TokenFilter {
	public static final Side DEFAULT_SIDE = Side.FRONT;
	public static final int DEFAULT_MAX_GRAM_SIZE = 1;
	public static final int DEFAULT_MIN_GRAM_SIZE = 1;

	public static int offset = 0;// temp offset
	public static boolean isFront = true;

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
	private char[] curTermBuffer;
	private int curTermLength;
	private int curGramSize;
	private int tokStart;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

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
		this.maxGram = 1000 * 100 * 100;
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
			if (curTermBuffer == null) {
				if (!input.incrementToken()) {
					return false;
				} else {
					curTermLength = termAtt.length();
					curTermBuffer = new char[curTermLength];
					System.arraycopy(termAtt.buffer(), 0, curTermBuffer, 0,
							curTermLength);
					curGramSize = minGram;
					tokStart = offsetAtt.startOffset();
				}
			}
			if (curGramSize <= maxGram) { // current gram length
				if (curGramSize >= curTermLength) {
					// if remaining input is too short, still generate
					clearAttributes();
					offsetAtt.setOffset(tokStart + 0, tokStart + curTermLength);
					termAtt.copyBuffer(curTermBuffer, 0, curTermLength);
					curTermBuffer = null;
					return true;
				} else {
					if (side == Side.FRONT || side == Side.BACK) {
						int start = side == Side.FRONT ? 0 : curTermLength
								- curGramSize;
						int end = start + curGramSize;
						clearAttributes();
						offsetAtt.setOffset(tokStart + start, tokStart + end);
						termAtt.copyBuffer(curTermBuffer, start, curGramSize);
						curGramSize++;
						return true;
					} else {
						int backStart = curTermLength - curGramSize;
						int backEnd = backStart + curGramSize;
						clearAttributes();
						if (isFront) {
							offsetAtt.setOffset(tokStart, tokStart
									+ curGramSize);
							termAtt.copyBuffer(curTermBuffer, 0, curGramSize);
							isFront = false;
						} else {
							offsetAtt.setOffset(tokStart + backStart, tokStart
									+ backEnd);
							termAtt.copyBuffer(curTermBuffer, backStart,
									curGramSize);
							isFront = true;
							curGramSize++;
						}
						return true;
					}
				}
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