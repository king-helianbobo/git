package org.elasticsearch.plugin;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.splitword.soul.analysis.BasicAnalysis;

public class SoulQueryAnalyzer extends Analyzer {

	public SoulQueryAnalyzer() {
		super();
	}
	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			final Reader reader) {
		Tokenizer tokenizer = new SoulTokenizer(new BasicAnalysis(reader),
				reader, EsStaticValue.filter, EsStaticValue.pstemming);
		TokenStream result = new StandardFilter(EsStaticValue.LuceneVersion,
				tokenizer);
		result = new SynonymTokenFilter(result, EsStaticValue.synonymTree);
		return new TokenStreamComponents(tokenizer, result);
	}
}
