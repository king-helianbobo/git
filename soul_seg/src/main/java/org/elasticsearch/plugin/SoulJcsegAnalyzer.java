package org.elasticsearch.plugin;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.splitword.lionsoul.jcseg.core.JcsegException;

public class SoulJcsegAnalyzer extends Analyzer {

	public Set<String> filter;
	public SoulJcsegAnalyzer() {
		super();
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			final Reader reader) {
		try {
			Tokenizer tokenizer = new SoulJcsegTokenizer(reader);
			TokenStream result = new StandardFilter(
					EsStaticValue.LuceneVersion, tokenizer);
			return new TokenStreamComponents(tokenizer, result);
		} catch (JcsegException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
