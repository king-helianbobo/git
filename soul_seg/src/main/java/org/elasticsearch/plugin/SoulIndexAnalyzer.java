package org.elasticsearch.plugin;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.soul.splitWord.BasicAnalysis;

public class SoulIndexAnalyzer extends Analyzer {

	boolean pstemming;
	public Set<String> filter;

	public SoulIndexAnalyzer(Set<String> filter, boolean pstemming) {
		this.filter = filter;
	}

	// 是否分析词干，进行单复数和时态的转换
	public SoulIndexAnalyzer(boolean pstemming) {
		this.pstemming = pstemming;
	}

	public SoulIndexAnalyzer() {
		super();
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			final Reader reader) {
		Tokenizer tokenizer = new SoulTokenizer(new BasicAnalysis(reader),
				reader, filter, pstemming);
		TokenStream result = new StandardFilter(Version.LUCENE_CURRENT,
				tokenizer);
		return new TokenStreamComponents(tokenizer, result);
	}

}
