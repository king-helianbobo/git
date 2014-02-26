package org.suggest.elasticsearch.action.refresh;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.suggest.elasticsearch.action.restful.RefreshRequestBuilder;

public class SuggestRefreshAction
		extends
			Action<SuggestRefreshRequest, SuggestRefreshResponse, RefreshRequestBuilder> {

	public static final SuggestRefreshAction INSTANCE = new SuggestRefreshAction();
	public static final String NAME = "suggestRefreshAction";

	private SuggestRefreshAction() {
		super(NAME);
	}

	@Override
	public RefreshRequestBuilder newRequestBuilder(Client client) {
		return new RefreshRequestBuilder(client);
	}

	@Override
	public SuggestRefreshResponse newResponse() {
		return new SuggestRefreshResponse();
	}

}
