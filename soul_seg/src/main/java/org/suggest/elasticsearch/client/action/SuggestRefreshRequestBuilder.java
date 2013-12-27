package org.suggest.elasticsearch.client.action;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalClient;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshAction;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshRequest;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshResponse;

public class SuggestRefreshRequestBuilder
		extends
			ActionRequestBuilder<SuggestRefreshRequest, SuggestRefreshResponse, SuggestRefreshRequestBuilder> {

	public SuggestRefreshRequestBuilder(Client client) {
		super((InternalClient) client, new SuggestRefreshRequest());
	}

	public SuggestRefreshRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	public SuggestRefreshRequestBuilder setField(String field) {
		request.field(field);
		return this;
	}

	@Override
	protected void doExecute(ActionListener<SuggestRefreshResponse> listener) {
		((Client) client).execute(SuggestRefreshAction.INSTANCE, request,
				listener);
	}

}
