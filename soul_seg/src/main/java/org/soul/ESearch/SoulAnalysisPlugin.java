package org.soul.ESearch;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class SoulAnalysisPlugin extends AbstractPlugin {

	@Override
	public String name() { // very important
		return "soul-analysis";
	}

	@Override
	public String description() {
		return "soul analysis";
	}

	@Override
	public void processModule(Module module) {
		if (module instanceof AnalysisModule) {
			AnalysisModule analysisModule = (AnalysisModule) module;
			analysisModule.addProcessor(new SoulAnalysisBinderProcessor());
		}
	}
}