package org.soul.elasticSearch.pinyin;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.settings.Settings;

import java.io.Reader;

public final class PinyinAnalyzer extends Analyzer {

	private String padding_char;
	private String first_letter;

	private int stringLen;

	public PinyinAnalyzer(Settings settings) {
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
		final WhitespaceTokenizer src = new WhitespaceTokenizer(
				Version.LUCENE_44, reader);
		TokenStream result = new StandardFilter(Version.LUCENE_44, src);
		result = new PinyinTokenFilter(result);
		// result = new SoulEdgeNGramTokenFilter(result,
		// SoulEdgeNGramTokenFilter.Side.FRONT, 1, 20);
		result = new SoulEdgeNGramTokenFilter(result,
				SoulEdgeNGramTokenFilter.Side.TWOSIDE, 1);
		return new TokenStreamComponents(src, result);
		// return new TokenStreamComponents(new SoulPinyinTokenizer(reader));
	}
}
