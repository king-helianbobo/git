package org.soul.elasticSearch.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.settings.Settings;
import org.soul.splitWord.BasicAnalysis;

import java.io.IOException;
import java.io.Reader;

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
				reader, ElasticSearchStaticVariable.filter, false);
		// 先进行soul分词，然后对每个Term再进行转换
		TokenStream result = new StandardFilter(Version.LUCENE_CURRENT,
				tokenizer);
		result = new PinyinTokenFilter(result);
		// result = new SoulEdgeNGramTokenFilter(result,
		// SoulEdgeNGramTokenFilter.Side.FRONT, 1, 20);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.TWOSIDE, 2);
		return new TokenStreamComponents(tokenizer, result);
	}
}
