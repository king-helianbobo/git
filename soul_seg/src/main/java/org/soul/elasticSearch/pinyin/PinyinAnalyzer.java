package org.soul.elasticSearch.pinyin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.io.Reader;

public class PinyinAnalyzer extends Analyzer {

	private static Log log = LogFactory.getLog(PinyinAnalyzer.class);

	private String padding_char;
	private String first_letter;

	private int stringLen;

	public PinyinAnalyzer(Settings settings) {
		log.info("PinyinAnalyzer is added!");
		first_letter = settings.get("first_letter", "none");
		padding_char = settings.get("padding_char", "");
	}

	public PinyinAnalyzer() {
		first_letter = "none";
		padding_char = " ";

	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(
				Version.LUCENE_CURRENT, reader);
		TokenStream result = new StandardFilter(Version.LUCENE_CURRENT,
				tokenizer);
		result = new PinyinTokenFilter(result);
		// // result = new SoulEdgeNGramTokenFilter(result,
		// // SoulEdgeNGramTokenFilter.Side.FRONT, 1, 20);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.TWOSIDE, 1);
		// log.info("another time , we get " + fieldName);
		return new TokenStreamComponents(tokenizer, result);
	}
}
