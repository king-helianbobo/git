package org.soul.lucene4;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.soul.splitWord.IndexAnalysis;

public class SoulIndexAnalysis extends Analyzer {

	boolean pstemming;
	public Set<String> filter;

	public SoulIndexAnalysis(Set<String> filter, boolean pstemming) {
		this.filter = filter;
	}

	/**
	 * @param pstemming
	 *            是否分析词干.进行单复数,时态的转换
	 */
	public SoulIndexAnalysis(boolean pstemming) {
		this.pstemming = pstemming;
	}

	public SoulIndexAnalysis() {
		super();
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			final Reader reader) {
		Tokenizer tokenizer = new SoulTokenizer(new IndexAnalysis(reader),
				reader, filter, pstemming);
		return new TokenStreamComponents(tokenizer);
	}

}
