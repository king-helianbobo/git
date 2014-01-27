package org.soul.elasticSearch.plugin;

import java.io.BufferedReader;
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
import org.soul.domain.Term;
import org.soul.domain.TermNature;
import org.soul.splitWord.Analysis;
import org.soul.utility.UserDefineLibrary;

public class SoulTokenizer extends Tokenizer {

	private static Log log = LogFactory.getLog(SoulTokenizer.class);
	// current word
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	// offset of current word
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	// word's number, 1st word,2nd word and so on
	private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);

	protected Analysis analysis = null;
	private Set<String> filter; // stop words
	private boolean pstemming;
	private final PorterStemmer stemmer = new PorterStemmer(); // 用于英文词干分析

	public SoulTokenizer(Analysis ta, Reader input, Set<String> filter,
			boolean pstemming) {
		super(input);
		this.analysis = ta;
		this.filter = filter;
		this.pstemming = pstemming;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		int position = 0;
		Term term = null;
		String name = null;
		int length = 0;
		boolean flag = true;
		do {
			term = analysis.next();
			if (term == null) {
				break;
			}
			name = term.getName();
			length = name.length();
			if (pstemming
					&& term.getTermNatures().termNatures[0] == TermNature.EN) {
				name = stemmer.stem(name);// 对英语进行词干分析
				term.setName(name);
			} else {
				// do nothing
			}
			if (filter != null && filter.contains(name)) {
				log.info("name " + name + " is filtered!");
				position++; // must keep its position
				continue;
			} else if (!StringUtils.hasText(name)) {
				// 如果字符串为连续空白，则忽略,但保留位置信息
				log.info("name " + name + " is whitespace!");
				position++; // must keep its position
				continue;
			} else {
				position++;
				flag = false;
			}
		} while (flag);
		if (term != null) {
			positionAttr.setPositionIncrement(position);
			termAtt.setEmpty().append(term.getName());
			offsetAtt.setOffset(term.getOffe(), term.getOffe() + length);
			return true;
		} else {
			return false;
		}
	}
	// must override this method, otherwise it will be fail when batch
	// processing index
	@Override
	public void reset() throws IOException {
		super.reset();
		analysis.resetContent(new BufferedReader(this.input));
	}

}
