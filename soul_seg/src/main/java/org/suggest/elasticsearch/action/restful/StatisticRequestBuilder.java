package org.suggest.elasticsearch.action.restful;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsAction;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsRequest;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsResponse;

public class StatisticRequestBuilder extends ActionRequestBuilder<SuggestStatisticsRequest, SuggestStatisticsResponse, StatisticRequestBuilder> {

    public StatisticRequestBuilder(Client client) {
        super((InternalGenericClient) client, new SuggestStatisticsRequest());
    }

    @Override
    protected void doExecute(ActionListener<SuggestStatisticsResponse> listener) {
        ((Client)client).execute(SuggestStatisticsAction.INSTANCE, request, listener);
    }
}
