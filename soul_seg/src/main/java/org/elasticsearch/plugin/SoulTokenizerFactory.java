package org.elasticsearch.plugin;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class SoulTokenizerFactory extends AbstractTokenizerFactory {

	public SoulTokenizerFactory(Index index, Settings indexSettings,
			String name, Settings settings) {
		super(index, indexSettings, name, settings);
	}

	@Override
	public Tokenizer create(Reader reader) {
		return null;
	}
}
