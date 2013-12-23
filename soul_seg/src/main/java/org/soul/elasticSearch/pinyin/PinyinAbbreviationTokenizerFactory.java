package org.soul.elasticSearch.pinyin;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

public class PinyinAbbreviationTokenizerFactory extends
		AbstractTokenizerFactory {
	@Inject
	public PinyinAbbreviationTokenizerFactory(Index index,
			@IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
	}

	@Override
	public Tokenizer create(Reader reader) {
		return new PinyinAbbreviationTokenizer(reader);
	}
}
