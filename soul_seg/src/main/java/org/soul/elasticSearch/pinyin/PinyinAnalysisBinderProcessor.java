/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.soul.elasticSearch.pinyin;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor.AnalyzersBindings;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenFiltersBindings;
import org.elasticsearch.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenizersBindings;

//负责添加analyzer
public class PinyinAnalysisBinderProcessor extends
		AnalysisModule.AnalysisBinderProcessor {

	@Override
	public void processAnalyzers(AnalyzersBindings analyzersBindings) {
		analyzersBindings.processAnalyzer("pinyin",
				PinyinAnalyzerProvider.class);
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
