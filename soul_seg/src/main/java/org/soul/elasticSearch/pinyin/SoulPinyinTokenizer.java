package org.soul.elasticSearch.pinyin;

import java.io.IOException;
import java.io.Reader;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class SoulPinyinTokenizer extends Tokenizer {
	private static final int DEFAULT_BUFFER_SIZE = 256;
	private static JcSegment seg = new JcSegment();

	private boolean done = false;
	private int finalOffset;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

	public SoulPinyinTokenizer(Reader reader) {
		this(reader, DEFAULT_BUFFER_SIZE);
	}

	public SoulPinyinTokenizer(Reader input, int bufferSize) {
		super(input);
		termAtt.resizeBuffer(bufferSize);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		if (!done) {
			done = true;
			// int upto = input.
			String pinyin = seg.segment(input);
			System.out.println("pinyin = " + pinyin);
			termAtt.resizeBuffer(pinyin.length());
			termAtt.setLength(pinyin.length());
			// System.out.println(str);
			termAtt.setEmpty();
			StringBuilder stringBuilder = new StringBuilder();
			StringBuilder firstLetters = new StringBuilder();
			termAtt.append(pinyin);
			// finalOffset = correctOffset(upto);
			// offsetAtt.setOffset(correctOffset(0), finalOffset);
			return true;
		}
		return false;
	}

	@Override
	public final void end() {
		offsetAtt.setOffset(finalOffset, finalOffset);
		try {
			super.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		this.done = false;
	}
}
