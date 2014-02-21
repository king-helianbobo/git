package org.elasticsearch.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.elasticsearch.common.settings.Settings;
import org.soul.analysis.BasicAnalysis;
import java.io.Reader;
import java.util.Set;

public class SoulPinyinAnalyzer extends Analyzer {

	private static Log log = LogFactory.getLog(SoulPinyinAnalyzer.class);

	public SoulPinyinAnalyzer(Settings settings) {
		log.info("SoulPinyinAnalyzer is added!");
	}

	public SoulPinyinAnalyzer() {
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		Tokenizer tokenizer = new SoulTokenizer(new BasicAnalysis(reader),
				reader, EsStaticValue.filter, EsStaticValue.pstemming);
		// first split this sentence ,then for each term use filter to convert
		TokenStream result = new StandardFilter(EsStaticValue.LuceneVersion,
				tokenizer);
		result = new PinyinTokenFilter(result, EsStaticValue.synonymTree);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.FRONT, 3);
		// result = new SoulEdgeNGramTokenFilter(result,
		// SoulEdgeNGramTokenFilter.Side.TWOSIDE, 2);
		return new TokenStreamComponents(tokenizer, result);
	}
}
