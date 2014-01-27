package org.soul.elasticsearch.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.cfg.SettingsManager;
import org.elasticsearch.hadoop.mr.MapReduceWriter;
import org.elasticsearch.hadoop.rest.InitializationUtils;
import org.elasticsearch.hadoop.rest.RestClient;
import org.elasticsearch.hadoop.serialization.IndexCommand;
import org.elasticsearch.hadoop.serialization.MapWritableIdExtractor;
import org.elasticsearch.hadoop.serialization.SerializationUtils;
import org.elasticsearch.hadoop.util.BytesArray;
import org.elasticsearch.hadoop.util.WritableUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lionsoul.jcseg.test.SogouDataReader;

public class HttpClientTest {
	private final Log log = LogFactory.getLog(HttpClientTest.class);
	private RestClient client;
	private Settings settings;
	TransportClient transportClient;
	private String indexName = "soul_test";
	private String typeName = "test1";
	private String hostName = "localhost";
	private int port = 9300;

	@Before
	public void startNode() throws Exception {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "number");
		String resource = indexName + "/" + typeName;
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
		// properties.put(ConfigurationOptions.ES_UPSERT_DOC, "false");
		properties.put("es.host", hostName);
		settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		client = new RestClient(settings);
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(hostName,
						port));
	}

	@After
	public void closeResources() {
		// DeleteIndexResponse deleteIndexResponse = transportClient.admin()
		// .indices().prepareDelete(indexName).execute().actionGet();
		// assertThat(deleteIndexResponse.isAcknowledged(), is(true));
		transportClient.close();
		client.close();
	}

	// @Ignore("Create Index and send data to index")
	@Test
	public void testMethod1() throws Exception {
		createIndexTestWithMapping();
		testIndexOperation();
	}
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
					.startObject("cardid").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("date").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("title").field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("search_analyzer", "soul_query").endObject()
					.startObject("content").field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("search_analyzer", "soul_query").endObject()
					.endObject().endObject().endObject().endObject();
			String settings = builder.string();
			// String settings = IOUtils.toString(NodeTestHelper.class
			// .getResourceAsStream("/http_test.json"));
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

	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		InputStream in = this.getClass().getResourceAsStream("/content2.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				"UTF-8"));
		List<Map<String, String>> result = SogouDataReader.getTestData(reader);
		BytesArray data = new BytesArray(16 * 1024);
		// data.bytes(new byte[settings.getBatchSizeInBytes()], 0);
		for (int i = 0; i < result.size(); i++) {
			Map<String, String> entry = result.get(i);
			MapWritable wr = (MapWritable) WritableUtils.toWritable(entry);
			int entrySize = command.prepare(wr);
			log.info(entrySize + "," + data.size() + "," + data.capacity());
			if (entrySize + data.size() > data.capacity()) {
				client.bulk(settings.getIndexType(), data.bytes(), data.size());
				data.reset();
			}
			command.write(wr, data);
		}
		client.bulk(settings.getIndexType(), data.bytes(), data.size());
	}
	@Ignore("Test Query")
	@Test
	public void testQueryStringOperation() {
		// 词组后面跟随~10,表示词组中的多个词之间的距离之和不超过10,则满足查询
		// 词之间的距离,即查询词组中词为满足和目标词组相同的最小移动次数
		String queryStr[] = {"\"耳熟能详\"", "\"微软并购雅虎\"", "\"微软是否\"~2",
				"\"微软是否\"~1", "Google互联网网上信息表"};
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
	@Ignore
	@Test
	public void testStopWordHaveEffectOperation() {
		// 词组后面跟随~10,表示词组中的多个词之间的距离之和不超过10,则满足查询
		// 词之间的距离,即查询词组中词为满足和目标词组相同的最小移动次数
		String queryStr[] = {"\"香港的网站为主，占\"", "\"香港的网站为主占\"", "\"香港的网站为主(占\"",
				"\"香港的网站为主()占\""};
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
			log.info("StopWordHaveEffectTest[" + str + "] ***********"
					+ searchResponse.toString());
		}
	}
	@Ignore("Test Query")
	@Test
	public void testSimpleQueryStringOperation() {
		// 使用soul_query分完词后，建立boolean查询，此时与顺序无关
		String queryStrs[] = {"Google雅虎", "Google雅虎责任编辑", "Google互联网", "雅虎"};
		for (String queryStr : queryStrs) {
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName)
					.setQuery(
							simpleQueryString(queryStr)
									.analyzer("soul_query")
									.field("content")
									.defaultOperator(
											SimpleQueryStringBuilder.Operator.AND))
					.get();

			log.info("SimpleQueryStringTest: [" + queryStr + "] ***********"
					+ searchResponse.toString());
		}
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
