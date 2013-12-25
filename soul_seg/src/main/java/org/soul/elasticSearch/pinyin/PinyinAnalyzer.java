package org.soul.elasticSearch.pinyin;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;

import java.io.Reader;

/**
 * Created by IntelliJ IDEA. User: Medcl' Date: 12-5-22 Time: 上午10:39
 */
public final class PinyinAnalyzer extends Analyzer {

	private String padding_char;
	private String first_letter;

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
		if (first_letter.equals("only")) {
			return new TokenStreamComponents(new PinyinAbbreviationTokenizer(
					reader));
		} else {
			return new TokenStreamComponents(new PinyinTokenizer(reader,
					padding_char, first_letter));
		}
	}

}
