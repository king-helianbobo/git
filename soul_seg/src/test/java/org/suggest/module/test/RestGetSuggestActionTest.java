package org.suggest.module.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
//import static org.suggest.module.test.NodeTestHelper.createNode;
//import static org.suggest.module.test.ProductTestHelper.createProducts;
//import static org.suggest.module.test.ProductTestHelper.indexProducts;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class RestGetSuggestActionTest {

	private final Log log = LogFactory.getLog(RestGetSuggestActionTest.class);

	private final AsyncHttpClient httpClient = new AsyncHttpClient();
	private Node node;
	private int port;

	@Before
	public void startNode() throws Exception {
		node = NodeTestHelper.createNode("foo", "fooNodeName", 1).call()
				.start();
		NodesInfoResponse response = node.client().admin().cluster()
				.prepareNodesInfo().setHttp(true).execute().actionGet();
		port = ((InetSocketTransportAddress) response.getNodes()[0].getHttp()
				.address().boundAddress()).address().getPort();
		log.info(port);
		List<Map<String, Object>> products = ProductTestHelper
				.createProducts(4);
		products.get(0).put("ProductName", "foo");
		products.get(1).put("ProductName", "foOb");
		products.get(2).put("ProductName", "fooBar");
		NodeTestHelper.createIndexWithMapping("products", node);
		ProductTestHelper.indexProducts(products, node);
		refreshAllSuggesters();
	}

	@After
	public void closeResources() {
		httpClient.close();
		node.client().close();
		node.close();
	}

	@Ignore("AsyncHttpClient does not allow body in GET requests - need to modify")
	@Test
	public void testThatSuggestionsShouldWorkWithGetRequestBody()
			throws Exception {
		String response = httpClient
				.prepareGet(
						"http://localhost:" + port
								+ "/products/product/__suggest")
				.setBody(createJSONQuery("ProductName.suggest", "foo", null))
				.execute().get().getResponseBody();
		// log.info(response);
		List<String> suggestions = getSuggestionsFromResponse(response);
		assertThat(suggestions, containsInAnyOrder("foo", "foob", "foobar"));
	}

	@Test
	public void testThatSuggestionsShouldWorkWithCallbackAndGetRequestParameter()
			throws Exception {
		// String query = URLEncoder.encode(
		// createJSONQuery("ProductName.suggest", "foobar"), "UTF8");
		String query = URLEncoder.encode(
				createJSONQuery("ProductName.lowercase", "foo", "fuzzy"),
				"UTF8");
		// String queryString = "callback=mycallback&source=" + query;
		String queryString = "pretty=true&source=" + query;
		log.info(queryString);
		String response = httpClient
				.prepareGet(
						"http://localhost:" + port
								+ "/products/product/__suggest?" + queryString)
				.execute().get().getResponseBody();
		List<String> suggestions = getSuggestionsFromResponse(response);
		assertThat(suggestions, containsInAnyOrder("foo", "foob", "foobar"));
		log.info(response);
		// assertThat(
		// response,
		// is("mycallback({\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0},\"suggestions\":[\"foobar\"]});"));
	}

	private void refreshAllSuggesters() throws Exception {
		Response r = httpClient
				.preparePost("http://localhost:" + port + "/__suggestRefresh")
				.execute().get();

		assertThat(r.getStatusCode(), is(200));
	}

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
