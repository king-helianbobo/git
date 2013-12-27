package org.suggest.elasticsearch.rest.action;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.action.support.RestActions.buildBroadcastShardsHeader;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.support.RestXContentBuilder;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsAction;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsRequest;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsResponse;

import java.io.IOException;

public class RestStatisticsAction extends BaseRestHandler {

	@Inject
	public RestStatisticsAction(Settings settings, Client client,
			RestController controller) {
		super(settings, client);
		controller.registerHandler(GET, "/__suggestStatistics", this);
	}

	@Override
	public void handleRequest(final RestRequest request,
			final RestChannel channel) {
		SuggestStatisticsRequest suggestStatisticsRequest = new SuggestStatisticsRequest();

		client.execute(SuggestStatisticsAction.INSTANCE,
				suggestStatisticsRequest,
				new ActionListener<SuggestStatisticsResponse>() {
					@Override
					public void onResponse(SuggestStatisticsResponse response) {
						try {
							XContentBuilder builder = RestXContentBuilder
									.restContentBuilder(request);
							builder.startObject();
							buildBroadcastShardsHeader(builder, response);
							response.fstStats().toXContent(builder, null);

							builder.endObject();
							channel.sendResponse(new XContentRestResponse(
									request, OK, builder));
						} catch (Exception e) {
							e.printStackTrace();
							onFailure(e);
						}
					}

					@Override
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
