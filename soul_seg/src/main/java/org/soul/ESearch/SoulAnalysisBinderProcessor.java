package org.soul.ESearch;

import org.elasticsearch.index.analysis.AnalysisModule;

public class SoulAnalysisBinderProcessor extends
		AnalysisModule.AnalysisBinderProcessor {

	@Override
	public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {

	}

	@Override
	public void processAnalyzers(AnalyzersBindings analyzersBindings) {
		analyzersBindings.processAnalyzer("soul_index",
				SoulIndexAnalyzerProvider.class);
		analyzersBindings.processAnalyzer("soul_query",
				SoulQueryAnalyzerProvider.class);
		super.processAnalyzers(analyzersBindings);
	}

	@Override
	public void processTokenizers(TokenizersBindings tokenizersBindings) {
		// tokenizersBindings.processTokenizer("ansj",
		// AnsjTokenizerFactory.class);
		// super.processTokenizers(tokenizersBindings);
	}
}
