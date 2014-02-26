package org.suggest.elasticsearch.action.restful;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;
import org.suggest.elasticsearch.action.termlist.TermlistAction;
import org.suggest.elasticsearch.action.termlist.TermlistRequest;
import org.suggest.elasticsearch.action.termlist.TermlistResponse;

/**
 * A request to get term lists of one or more indices. This class no effect at
 * present
 */
public class TermlistRequestBuilder
		extends
			BroadcastOperationRequestBuilder<TermlistRequest, TermlistResponse, TermlistRequestBuilder> {

	public TermlistRequestBuilder(InternalGenericClient client) {
		super(client, new TermlistRequest());
	}

	public TermlistRequestBuilder withDocFreq() {
		request.setWithDocFreq(true);
		return this;
	}

	public TermlistRequestBuilder withTotalFreq() {
		request.setWithTotalFreq(true);
		return this;
	}

	@Override
	protected void doExecute(ActionListener<TermlistResponse> listener) {
		((Client) client).execute(TermlistAction.INSTANCE, request, listener);
	}
}
