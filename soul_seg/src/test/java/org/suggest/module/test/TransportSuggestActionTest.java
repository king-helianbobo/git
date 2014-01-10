package org.suggest.module.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

import org.elasticsearch.common.Strings;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshAction;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshRequest;
import org.suggest.elasticsearch.action.statistics.FstStats;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsAction;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsRequest;
import org.suggest.elasticsearch.action.suggest.SuggestAction;
import org.suggest.elasticsearch.action.suggest.SuggestRequest;
import org.suggest.elasticsearch.action.suggest.SuggestResponse;

import java.util.List;

@RunWith(value = Parameterized.class)
public class TransportSuggestActionTest extends AbstractSuggestTest {

	public TransportSuggestActionTest(int shards, int nodeCount)
			throws Exception {
		super(shards, nodeCount);
		// value come from AbstractSuggestTest
	}

	@Override
	public List<String> getSuggestions(SuggestionQuery suggestionQuery)
			throws Exception {
		SuggestRequest request = new SuggestRequest(suggestionQuery.index);

		request.term(suggestionQuery.term);
		request.field(suggestionQuery.field);

		if (suggestionQuery.size != null) {
			request.size(suggestionQuery.size);
		}
		if (suggestionQuery.similarity != null
				&& suggestionQuery.similarity > 0.0
				&& suggestionQuery.similarity < 1.0) {
			request.similarity(suggestionQuery.similarity);
		}
		if (suggestionQuery.suggestType != null) {
			request.suggestType(suggestionQuery.suggestType);
		}
		if (Strings.hasLength(suggestionQuery.indexAnalyzer)) {
			request.indexAnalyzer(suggestionQuery.indexAnalyzer);
		}
		if (Strings.hasLength(suggestionQuery.queryAnalyzer)) {
			request.queryAnalyzer(suggestionQuery.queryAnalyzer);
		}
		if (Strings.hasLength(suggestionQuery.analyzer)) {
			request.analyzer(suggestionQuery.analyzer);
		}

		request.preservePositionIncrements(suggestionQuery.preservePositionIncrements);

		SuggestResponse suggestResponse = node.client()
				.execute(SuggestAction.INSTANCE, request).actionGet();
		assertThat(suggestResponse.getShardFailures(), is(emptyArray()));

		return suggestResponse.suggestions();
	}

	@Override
	public void refreshAllSuggesters() throws Exception {
		SuggestRefreshRequest refreshRequest = new SuggestRefreshRequest();
		node.client().execute(SuggestRefreshAction.INSTANCE, refreshRequest)
				.actionGet();
	}

	@Override
	public void refreshIndexSuggesters(String index) throws Exception {
		SuggestRefreshRequest refreshRequest = new SuggestRefreshRequest(index);
		node.client().execute(SuggestRefreshAction.INSTANCE, refreshRequest)
				.actionGet();
	}

	@Override
	public void refreshFieldSuggesters(String index, String field)
			throws Exception {
		SuggestRefreshRequest refreshRequest = new SuggestRefreshRequest(index);
		refreshRequest.field(field);
		node.client().execute(SuggestRefreshAction.INSTANCE, refreshRequest)
				.actionGet();
	}

	@Override
	public FstStats getStatistics() throws Exception {
		SuggestStatisticsRequest suggestStatisticsRequest = new SuggestStatisticsRequest();
		return node
				.client()
				.execute(SuggestStatisticsAction.INSTANCE,
						suggestStatisticsRequest).actionGet().fstStats();
	}

}
