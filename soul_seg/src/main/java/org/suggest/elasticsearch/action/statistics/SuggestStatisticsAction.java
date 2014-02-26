package org.suggest.elasticsearch.action.statistics;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.suggest.elasticsearch.action.restful.StatisticRequestBuilder;

public class SuggestStatisticsAction
		extends
			Action<SuggestStatisticsRequest, SuggestStatisticsResponse, StatisticRequestBuilder> {

	public static final SuggestStatisticsAction INSTANCE = new SuggestStatisticsAction();
	public static final String NAME = "suggestStatisticsAction";

	private SuggestStatisticsAction() {
		super(NAME);
	}

	@Override
	public StatisticRequestBuilder newRequestBuilder(Client client) {
		return new StatisticRequestBuilder(client);
	}

	@Override
	public SuggestStatisticsResponse newResponse() {
		return new SuggestStatisticsResponse();
	}
}
