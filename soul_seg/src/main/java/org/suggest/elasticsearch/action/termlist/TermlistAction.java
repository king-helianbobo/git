package org.suggest.elasticsearch.action.termlist;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;
import org.suggest.elasticsearch.client.action.TermlistRequestBuilder;

/**
 * The action class TermlistAction is a glue code that clamps together
 * TermlistRequest, TermlistResponse, and a TermlistRequestBuilder into a
 * logical unit. All we need to do is taking care of the Generics of the class
 * and filling out the instantiation helpers newRequestBuilder() and
 * newResponse(). From now on, the ElasticSearch internal client knows how to
 * construct the request and response structures for our action. But up to this
 * point, no shard logic is involved.
 * 
 * @author liubo
 * 
 */
public class TermlistAction
		extends
			Action<TermlistRequest, TermlistResponse, TermlistRequestBuilder> {

	public static final TermlistAction INSTANCE = new TermlistAction(); // singleton

	public static final String NAME = "indices/termlist";

	private TermlistAction() {
		super(NAME);
	}

	@Override
	public TermlistResponse newResponse() {
		return new TermlistResponse();
	}

	@Override
	public TermlistRequestBuilder newRequestBuilder(Client client) {
		return null;
		// return new TermlistRequestBuilder((InternalGenericClient) client);
	}
}
