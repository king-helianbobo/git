package org.soul.elasticSearch.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.soul.elasticSearch.pinyin.PinyinAnalyzerProvider;

public class SoulAnalysisBinderProcessor
		extends
			AnalysisModule.AnalysisBinderProcessor {
	private static Log log = LogFactory
			.getLog(SoulAnalysisBinderProcessor.class);
	@Override
	public void processAnalyzers(AnalyzersBindings analyzersBindings) {
		analyzersBindings.processAnalyzer("soul_index",
				SoulIndexAnalyzerProvider.class);
		analyzersBindings.processAnalyzer("soul_query",
				SoulQueryAnalyzerProvider.class);
		analyzersBindings.processAnalyzer("soul_pinyin",
				PinyinAnalyzerProvider.class);
		super.processAnalyzers(analyzersBindings);
	}
}
