package org.soul.elasticsearch.module.suggest.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.suggest.elasticsearch.action.statistics.FstStats;
import org.suggest.elasticsearch.action.suggest.SuggestResponse;
import org.suggest.elasticsearch.client.action.SuggestRefreshRequestBuilder;
import org.suggest.elasticsearch.client.action.SuggestRequestBuilder;
import org.suggest.elasticsearch.client.action.SuggestStatisticsRequestBuilder;

@RunWith(value = Parameterized.class)
public class TransportClientTest extends AbstractSuggestTest {

    public TransportClientTest(int shards, int nodeCount) throws Exception {
        super(shards, nodeCount);
    }

    private TransportClient client;

    @Before
    public void startElasticSearch() throws IOException {
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).put("node.client", true).build();
        client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
    }

    @After
    public void closeClient() throws Exception {
        client.close();
    }

    @Override
    public List<String> getSuggestions(SuggestionQuery suggestionQuery) throws Exception {
        SuggestRequestBuilder builder = new SuggestRequestBuilder(client)
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
        if (Strings.hasLength(suggestionQuery.queryAnalyzer)) {
            builder.queryAnalyzer(suggestionQuery.queryAnalyzer);
        }
        if (Strings.hasLength(suggestionQuery.indexAnalyzer)) {
            builder.indexAnalyzer(suggestionQuery.indexAnalyzer);
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
        SuggestRefreshRequestBuilder builder = new SuggestRefreshRequestBuilder(client);
        builder.execute().actionGet();
    }

    @Override
    public void refreshIndexSuggesters(String index) throws Exception {
        SuggestRefreshRequestBuilder builder = new SuggestRefreshRequestBuilder(client).setIndices(index);
        builder.execute().actionGet();
    }

    @Override
    public void refreshFieldSuggesters(String index, String field) throws Exception {
        SuggestRefreshRequestBuilder builder = new SuggestRefreshRequestBuilder(client).setIndices(index).setField(field);
        builder.execute().actionGet();
    }

    @Override
    public FstStats getStatistics() throws Exception {
        SuggestStatisticsRequestBuilder builder = new SuggestStatisticsRequestBuilder(client);
        return builder.execute().actionGet().fstStats();
    }
}
