package org.elasticsearch.plugin;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.soul.analysis.BasicAnalysis;

public class SoulIndexAnalyzer extends Analyzer {

	boolean pstemming = EsStaticValue.pstemming;
	// 是否分析词干，进行单复数和时态的转换
	public Set<String> filter = EsStaticValue.filter;

	public SoulIndexAnalyzer() {
		super();
	}
	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			final Reader reader) {
		Tokenizer tokenizer = new SoulTokenizer(new BasicAnalysis(reader),
				reader, filter, pstemming);
		TokenStream result = new StandardFilter(EsStaticValue.LuceneVersion,
				tokenizer);
		return new TokenStreamComponents(tokenizer, result);
	}
}
