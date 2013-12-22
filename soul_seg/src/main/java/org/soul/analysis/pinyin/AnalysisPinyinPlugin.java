package org.soul.analysis.pinyin;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

/**
 * The Pinyin Analysis plugin integrates
 * Pinyin4j(http://pinyin4j.sourceforge.net/) module into elasticsearch.
 */
public class AnalysisPinyinPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "analysis-pinyin";
	}

	@Override
	public String description() {
		return "Chinese word to Pinyin convert support";
	}

	public void onModule(AnalysisModule module) {
		module.addProcessor(new PinyinAnalysisBinderProcessor());
	}
}
