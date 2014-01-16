package org.soul.elasticsearch.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EdgeNGramTest {
	TransportClient transportClient;
	private final Log log = LogFactory.getLog(EdgeNGramTest.class);
	private String indexName = "test";
	private String typeName = "test1";
	private int port = 9300;

	@Before
	public void startClient() throws Exception {
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", port));
	}
	@After
	public void closeClient() {
		transportClient.close();
	}
	@Ignore
	@Test
	public void createIndexTestWithMapping() {
		try {
			IndicesExistsResponse existsResponse = transportClient.admin()
					.indices().prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				transportClient.admin().indices().prepareDelete(indexName)
						.execute().actionGet();
			}
			XContentBuilder builder = (XContentBuilder) jsonBuilder()
					.startObject().startObject("mappings")
					.startObject(typeName).startObject("properties")
					.startObject("content").field("type", "string")
					.field("index_analyzer", "soul_pinyin")
					.field("search_analyzer", "whitespace").endObject()
					.endObject().endObject().endObject().endObject();
			String settings = builder.string();
			log.info(settings);
			CreateIndexResponse createIndexResponse = transportClient.admin()
					.indices().prepareCreate(indexName).setSource(settings)
					.execute().actionGet();
			assertThat(createIndexResponse.isAcknowledged(), is(true));
			transportClient.admin().cluster().prepareHealth(indexName)
					.setWaitForGreenStatus().execute().actionGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Ignore
	@Test
	public void testIndexOperation() throws Exception {
		String[] strs = {"沈阳", "沈阳市长", "沈阳大学", "沈从文"};
		for (int i = 0; i < strs.length; i++) {
			transportClient
					.prepareIndex(indexName, typeName, String.valueOf((i + 1)))
					.setSource("content", strs[i]).get();
		}
	}
	// @Ignore
	@Test
	public void queryStringTest() {
		String queryStr[] = {"shencong", "shenc", "沈阳", "sheny"};
		for (String str : queryStr) {
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName)
					.setQuery(
							QueryBuilders
									.queryString(str)
									.defaultField("content")
									.defaultOperator(
											QueryStringQueryBuilder.Operator.AND))
					.get();
			log.info("QueryStringTest[" + str + "] ***********"
					+ searchResponse.toString());
		}
	}

}
