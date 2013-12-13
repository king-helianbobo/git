package org.wltea.index.analysis;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;
import org.wltea.analyzer.core.Configuration;
import org.wltea.analyzer.core.Dictionary;
import org.wltea.analyzer.core.IKAnalyzer;

public class IkAnalyzerProvider extends
		AbstractIndexAnalyzerProvider<IKAnalyzer> {
	private final IKAnalyzer analyzer;

	@Inject
	public IkAnalyzerProvider(Index index,
			@IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		Dictionary.initial(new Configuration(env));
		analyzer = new IKAnalyzer(indexSettings, settings, env);
	}

	@Override
	public IKAnalyzer get() {
		return this.analyzer;
	}
}
