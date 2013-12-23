package org.soul.elasticSearch.plugin;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenFiltersBindings;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenizersBindings;
import org.soul.elasticSearch.pinyin.PinyinAbbreviationTokenizerFactory;
import org.soul.elasticSearch.pinyin.PinyinAnalyzerProvider;
import org.soul.elasticSearch.pinyin.PinyinTokenFilterFactory;
import org.soul.elasticSearch.pinyin.PinyinTokenizerFactory;

public class SoulAnalysisBinderProcessor extends
		AnalysisModule.AnalysisBinderProcessor {

	@Override
	public void processAnalyzers(AnalyzersBindings analyzersBindings) {
		analyzersBindings.processAnalyzer("soul_index",
				SoulIndexAnalyzerProvider.class);
		analyzersBindings.processAnalyzer("soul_query",
				SoulQueryAnalyzerProvider.class);
		analyzersBindings.processAnalyzer("pinyin",
				PinyinAnalyzerProvider.class);
		super.processAnalyzers(analyzersBindings);
	}

	@Override
	public void processTokenizers(TokenizersBindings tokenizersBindings) {
		tokenizersBindings.processTokenizer("pinyin",
				PinyinTokenizerFactory.class); // 拼音分词器
		tokenizersBindings.processTokenizer("pinyin_first_letter",
				PinyinAbbreviationTokenizerFactory.class); // 拼音简写工具
	}

	@Override
	public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
		// token filter
		tokenFiltersBindings.processTokenFilter("pinyin",
				PinyinTokenFilterFactory.class);
	}

}
