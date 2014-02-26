package org.suggest.module.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

import org.elasticsearch.common.Strings;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.suggest.elasticsearch.action.restful.RefreshRequestBuilder;
import org.suggest.elasticsearch.action.restful.StatisticRequestBuilder;
import org.suggest.elasticsearch.action.restful.SuggestRequestBuilder;
import org.suggest.elasticsearch.action.statistics.FstStats;
import org.suggest.elasticsearch.action.suggest.SuggestResponse;

import java.util.List;

@RunWith(value = Parameterized.class)
public class SuggestBuildersTest extends AbstractSuggestTest {

    public SuggestBuildersTest(int shards, int nodeCount) throws Exception {
        super(shards, nodeCount);
    }

    @Override
    public List<String> getSuggestions(SuggestionQuery suggestionQuery) throws Exception {
        SuggestRequestBuilder builder = new SuggestRequestBuilder(node.client())
                .setIndices(suggestionQuery.index)
                .field(suggestionQuery.field)
                .term(suggestionQuery.term);

        if (suggestionQuery.size != null) {
            builder.size(suggestionQuery.size);
        }
        if (suggestionQuery.similarity != null && suggestionQuery.similarity > 0.0 && suggestionQuery.similarity < 1.0) {
            builder.similarity(suggestionQuery.similarity);
        }
        if (suggestionQuery.suggestType != null) {
            builder.suggestType(suggestionQuery.suggestType);
        }
        if (Strings.hasLength(suggestionQuery.indexAnalyzer)) {
            builder.indexAnalyzer(suggestionQuery.indexAnalyzer);
        }
        if (Strings.hasLength(suggestionQuery.queryAnalyzer)) {
            builder.queryAnalyzer(suggestionQuery.queryAnalyzer);
        }
        if (Strings.hasLength(suggestionQuery.analyzer)) {
            builder.analyzer(suggestionQuery.analyzer);
        }
        builder.preservePositionIncrements(suggestionQuery.preservePositionIncrements);

        SuggestResponse suggestResponse = builder.execute().actionGet();
        assertThat(suggestResponse.getShardFailures(), is(emptyArray()));

        return suggestResponse.suggestions();
    }

    @Override
    public void refreshAllSuggesters() throws Exception {
        RefreshRequestBuilder builder = new RefreshRequestBuilder(node.client());
        builder.execute().actionGet();
    }

    @Override
    public void refreshIndexSuggesters(String index) throws Exception {
        RefreshRequestBuilder builder = new RefreshRequestBuilder(node.client()).setIndices(index);
        builder.execute().actionGet();
    }

    @Override
    public void refreshFieldSuggesters(String index, String field) throws Exception {
        RefreshRequestBuilder builder = new RefreshRequestBuilder(node.client()).setIndices(index).setField(field);
        builder.execute().actionGet();
    }

    @Override
    public FstStats getStatistics() throws Exception {
        StatisticRequestBuilder builder = new StatisticRequestBuilder(node.client());
        return builder.execute().actionGet().fstStats();
    }
}
