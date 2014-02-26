package org.suggest.elasticsearch.action.restful;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

import org.suggest.elasticsearch.action.termlist.TermInfoPojo;
import org.suggest.elasticsearch.action.termlist.TermlistAction;
import org.suggest.elasticsearch.action.termlist.TermlistRequest;
import org.suggest.elasticsearch.action.termlist.TermlistResponse;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestActions.buildBroadcastShardsHeader;

public class RestTermlistAction extends BaseRestHandler {

	@Inject
	public RestTermlistAction(Settings settings, Client client,
			RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/_termlist", this);
		controller.registerHandler(GET, "/{index}/_termlist", this);
		controller.registerHandler(GET, "/{index}/{field}/_termlist", this);
	}

	public void handleRequest(final RestRequest request,
			final RestChannel channel) {
		TermlistRequest termlistRequest = new TermlistRequest(
				Strings.splitStringByCommaToArray(request.param("index")));
		termlistRequest.setField(request.param("field"));
		termlistRequest.setSize(request.paramAsInt("size", 0));
		termlistRequest
				.setWithDocFreq(request.paramAsBoolean("docfreq", false));
		termlistRequest.setWithTotalFreq(request.paramAsBoolean("totalfreq",
				false));
		client.execute(TermlistAction.INSTANCE, termlistRequest,
				new ActionListener<TermlistResponse>() {
					public void onResponse(TermlistResponse response) {
						try {
							XContentBuilder builder = RestXContentBuilder
									.restContentBuilder(request);
							builder.startObject();
							buildBroadcastShardsHeader(builder, response);
							builder.startArray("termList");
							for (Map.Entry<String, TermInfoPojo> t : response
									.getTermlist().entrySet()) {
								builder.startObject().field("fieldContent",
										t.getKey());
								if (t.getValue().getDocFreq() != null) {
									builder.field("docFreq", t.getValue()
											.getDocFreq());
								}
								if (t.getValue().getTotalFreq() != null) {
									builder.field("totalFreq", t.getValue()
											.getTotalFreq());
								}
								builder.endObject();
							}
							builder.endArray().endObject();
							channel.sendResponse(new XContentRestResponse(
									request, OK, builder));
						} catch (Exception e) {
							onFailure(e);
						}
					}

					public void onFailure(Throwable e) {
						try {
							channel.sendResponse(new XContentThrowableRestResponse(
									request, e));
						} catch (IOException e1) {
							logger.error("Failed to send failure response", e1);
						}
					}
				});
	}
}