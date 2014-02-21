package org.elasticsearch.plugin;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

public class SoulPinyinAnalyzerProvider
		extends
			AbstractIndexAnalyzerProvider<SoulPinyinAnalyzer> {

	private final SoulPinyinAnalyzer analyzer;

	@Inject
	public SoulPinyinAnalyzerProvider(Index index,
			@IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new SoulPinyinAnalyzer(settings);
	}

	@Override
	public SoulPinyinAnalyzer get() {
		return this.analyzer;
	}
}
