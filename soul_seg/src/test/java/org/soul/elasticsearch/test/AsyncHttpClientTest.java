package org.soul.elasticsearch.test;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.cfg.SettingsManager;
import org.elasticsearch.hadoop.mr.MapReduceWriter;
import org.elasticsearch.hadoop.rest.InitializationUtils;
import org.elasticsearch.hadoop.rest.Resource;
import org.elasticsearch.hadoop.rest.RestClient;
import org.elasticsearch.hadoop.serialization.Command;
import org.elasticsearch.hadoop.serialization.IndexCommand;
import org.elasticsearch.hadoop.serialization.MapWritableIdExtractor;
import org.elasticsearch.hadoop.serialization.SerializationUtils;
import org.elasticsearch.hadoop.util.BytesArray;
import org.elasticsearch.hadoop.util.WritableUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AsyncHttpClientTest {
	private final Log log = LogFactory.getLog(AsyncHttpClientTest.class);
	private RestClient client;
	private Settings settings;

	private String resource = "http_test/test1";

	// private final AsyncHttpClient httpClient = new AsyncHttpClient();
	// private Node node;
	private int port;

	@Before
	public void startNode() throws Exception {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "number");
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
		// properties.put(ConfigurationOptions.ES_UPSERT_DOC, "false");
		properties.put("es.host", "localhost");
		settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		client = new RestClient(settings);
	}
	@After
	public void closeResources() {
		client.close();
	}

	@Ignore("Index")
	@Test
	public void testIndexOperation() throws Exception {

		IndexCommand command = new IndexCommand(settings);

		Map<String, String> entry = new LinkedHashMap<String, String>();
		entry.put("number", "2");
		entry.put("cardid", "010086");
		entry.put("playdate", "2014-01-10");
		entry.put("channel", "中华人民共和国");
		entry.put("program", "中华民国的国庆节是公元1911年10月10日");
		MapWritable wr = (MapWritable) WritableUtils.toWritable(entry);
		int size = command.prepare(wr);
		BytesArray data = new BytesArray(1024);
		command.write(wr, data);
		log.info(data);
		log.info(settings.getTargetResource());
		client.bulk(settings.getTargetResource(), data.bytes(), data.size());
		// log.info("data.size = " + data.size());
		data.reset();

	}

	@Test
	public void testQueryOperation() {

		TransportClient client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));

		SearchResponse searchResponse = client
				.prepareSearch("http_test")
				.setQuery(
						QueryBuilders.queryString("\"中华民国\"").defaultField(
								"program")).get();
		log.info(searchResponse.toString());
		assertHitCount(searchResponse, 2);
		log.info(searchResponse.getHits().getAt(1).getMatchedQueries());
	}

	// @Test
	// public void
	// testThatSuggestionsShouldWorkWithCallbackAndGetRequestParameter()
	// throws Exception {
	// // String query = URLEncoder.encode(
	// // createJSONQuery("ProductName.suggest", "foobar"), "UTF8");
	// String query = URLEncoder.encode(
	// createJSONQuery("ProductName.lowercase", "foo", "fuzzy"),
	// "UTF8");
	// // String queryString = "callback=mycallback&source=" + query;
	// String queryString = "pretty=true&source=" + query;
	// log.info(queryString);
	// String response = httpClient
	// .prepareGet(
	// "http://localhost:" + port
	// + "/products/product/__suggest?" + queryString)
	// .execute().get().getResponseBody();
	// List<String> suggestions = getSuggestionsFromResponse(response);
	// assertThat(suggestions, containsInAnyOrder("foo", "foob", "foobar"));
	// log.info(response);
	// // assertThat(
	// // response,
	// //
	// is("mycallback({\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0},\"suggestions\":[\"foobar\"]});"));
	// }

	// private void refreshAllSuggesters() throws Exception {
	// Response r = httpClient
	// .preparePost("http://localhost:" + port + "/__suggestRefresh")
	// .execute().get();
	//
	// assertThat(r.getStatusCode(), is(200));
	// }

	private String createJSONQuery(String field, String term, String type) {
		if (type == null)
			return String.format("{ \"field\": \"%s\", \"term\": \"%s\" }",
					field, term);
		else
			return String
					.format("{ \"field\": \"%s\", \"term\": \"%s\" , \"type\": \"%s\" }",
							field, term, type);
	}

	@SuppressWarnings("unchecked")
	private List<String> getSuggestionsFromResponse(String response)
			throws IOException {
		XContentParser parser = JsonXContent.jsonXContent
				.createParser(response);
		Map<String, Object> jsonResponse = parser.map();
		assertThat(jsonResponse, hasKey("suggestions"));
		return (List<String>) jsonResponse.get("suggestions");
	}

}
