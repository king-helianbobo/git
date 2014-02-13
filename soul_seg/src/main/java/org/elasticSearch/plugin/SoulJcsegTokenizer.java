package org.elasticSearch.plugin;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.elasticsearch.hadoop.util.StringUtils;
import org.lionsoul.jcseg.core.ADictionary;
import org.lionsoul.jcseg.core.DictionaryFactory;
import org.lionsoul.jcseg.core.ISegment;
import org.lionsoul.jcseg.core.IWord;
import org.lionsoul.jcseg.core.JcsegException;
import org.lionsoul.jcseg.core.JcsegTaskConfig;
import org.lionsoul.jcseg.core.SegmentFactory;
import org.soul.domain.TermNature;

public class SoulJcsegTokenizer extends Tokenizer {
	private static Log log = LogFactory.getLog(SoulJcsegTokenizer.class);
	private ISegment segmentor = null;
	private Set<String> filter = null; // stop words
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posAttr = addAttribute(PositionIncrementAttribute.class);

	public SoulJcsegTokenizer(Reader reader) throws JcsegException, IOException {
		super(reader);
		JcsegTaskConfig conf = new JcsegTaskConfig();
		ADictionary dic = DictionaryFactory.createDefaultDictionary(conf);
		try {
			segmentor = SegmentFactory.createJcseg(
					JcsegTaskConfig.COMPLEX_MODE, new Object[]{conf, dic});
		} catch (JcsegException e) {
			e.printStackTrace();
		}
		segmentor.reset(reader);
	}
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		// IWord word = segmentor.next();
		int length = 0;
		IWord word = null;
		boolean flag = true;
		int numWhiteSpace = 0;
		int position = 0;
		do {
			word = segmentor.next();
			if (word == null) {
				break;
			}
			String text = word.getValue();
			length = text.length();
			if (filter != null && filter.contains(text)) {
				if (numWhiteSpace > 0) {
					position++;
					numWhiteSpace = 0;
				}
				// see continuous blankSpace as blankSpace
				position++; // keep its position
				continue;
			} else if (!StringUtils.hasText(text)) {
				numWhiteSpace++;
				// see continuous blankSpace as blankSpace ,keep its position
				// log.info("name " + name + " is whitespace!");
				// keep its position
				continue;
			} else {
				if (numWhiteSpace > 0) {
					position++;
					numWhiteSpace = 0;
				}
				position++;
				flag = false;
			}

		} while (flag);
		if (word != null) {
			posAttr.setPositionIncrement(position);
			termAtt.setEmpty().append(word.getValue());
			log.info("JcSeg: [" + word.getValue() + "]" + "position: "
					+ word.getPosition() + " length: " + word.getLength());
			// termAtt.copyBuffer(word.getValue(), 0,
			// word.getValue().length);
			// termAtt.setLength(word.getLength());
			offsetAtt.setOffset(word.getPosition(),
					word.getPosition() + word.getLength());
			return true;
		} else {
			end();
			return false;
		}
	}
	@Override
	public void reset() throws IOException {
		super.reset();
		segmentor.reset(input);
	}
}
