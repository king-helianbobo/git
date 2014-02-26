package org.suggest.elasticsearch.action.restful;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalClient;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshAction;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshRequest;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshResponse;

public class RefreshRequestBuilder
		extends
			ActionRequestBuilder<SuggestRefreshRequest, SuggestRefreshResponse, RefreshRequestBuilder> {

	public RefreshRequestBuilder(Client client) {
		super((InternalClient) client, new SuggestRefreshRequest());
	}

	@Override
	protected void doExecute(ActionListener<SuggestRefreshResponse> listener) {
		((Client) client).execute(SuggestRefreshAction.INSTANCE, request,
				listener);
	}

	public RefreshRequestBuilder setIndices(String... indices) {
		request.indices(indices);
		return this;
	}

	public RefreshRequestBuilder setField(String field) {
		request.field(field);
		return this;
	}
}
