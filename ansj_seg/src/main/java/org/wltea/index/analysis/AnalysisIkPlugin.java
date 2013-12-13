package org.wltea.index.analysis;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisIkPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "analysis-ik";
	}

	@Override
	public String description() {
		return "ik analysis";
	}

	@Override
	public void processModule(Module module) {
		if (module instanceof AnalysisModule) {
			AnalysisModule analysisModule = (AnalysisModule) module;
			analysisModule.addProcessor(new IkAnalysisBinderProcessor());
		}
	}
}
