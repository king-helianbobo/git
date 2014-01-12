package org.soul.elasticsearch.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
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
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
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
import org.soul.domain.Term;
import org.soul.splitWord.BasicAnalysis;
import org.soul.treeSplit.IOUtil;
import org.suggest.module.test.NodeTestHelper;

public class AsyncHttpClientTest {
	private final Log log = LogFactory.getLog(AsyncHttpClientTest.class);
	private RestClient client;
	private Settings settings;
	TransportClient transportClient;

	private String indexName = "http_test";
	private String typeName = "test1";

	// private final AsyncHttpClient httpClient = new AsyncHttpClient();
	// private Node node;
	private int port = 9300;

	@Before
	public void startNode() throws Exception {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "number");
		String resource = indexName + "/" + typeName;
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
		// properties.put(ConfigurationOptions.ES_UPSERT_DOC, "false");
		properties.put("es.host", "localhost");
		settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		client = new RestClient(settings);
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", port));

	}

	@After
	public void closeResources() {
		// DeleteIndexResponse deleteIndexResponse = transportClient.admin()
		// .indices().prepareDelete(indexName).execute().actionGet();
		// assertThat(deleteIndexResponse.isAcknowledged(), is(true));
		transportClient.close();
		client.close();
	}

	@Ignore("Create Index")
	@Test
	public void createIndexTestWithMapping() {
		try {
			IndicesExistsResponse existsResponse = transportClient.admin()
					.indices().prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				transportClient.admin().indices().prepareDelete(indexName)
						.execute().actionGet();
			}
			String settings = IOUtils.toString(NodeTestHelper.class
					.getResourceAsStream("/http_test.json"));
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

	@Ignore("Index Operation")
	@Test
	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		String filePath = "/mnt/f/tmp/content3.txt";
		List<Map<String, String>> result = getTestData(filePath);
		BytesArray data = new BytesArray(4096);
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

	// @Ignore("Test Query")
	@Test
	public void testQueryOperation() {
		String settings;
		try {
			XContentBuilder builder = (XContentBuilder) jsonBuilder()
					.startObject().startObject("mappings")
					.startObject(typeName).startObject("properties")
					.startObject("cardid").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("playdate").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("channel").field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("query_analyzer", "soul_query").endObject()
					.startObject("program").field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("query_analyzer", "soul_query").endObject()
					.endObject().endObject().endObject().endObject();
			String str = builder.string();
			log.info(str);
			settings = IOUtils.toString(NodeTestHelper.class
					.getResourceAsStream("/http_test.json"));
			log.info(settings);
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName)
					.setQuery(
							QueryBuilders.queryString("\"中华民国\"").defaultField(
									"program")).get();
			log.info(searchResponse.toString());
			assertHitCount(searchResponse, 2);
			log.info(searchResponse.getHits().getAt(1).getMatchedQueries());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<Map<String, String>> getTestData(String path)
			throws IOException {
		List<Map<String, String>> products = Lists.newArrayList();
		String temp = null;
		BufferedReader reader = IOUtil.getReader(path, "UTF-8");
		int number = 0;
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			if (temp.startsWith("<content>")) {
				int end = temp.lastIndexOf("</content>");
				if (end >= 0) {
					String content = temp.substring("<content>".length(), end);
					if (content.length() > 0) {
						number++;
						Map<String, String> entry = new LinkedHashMap<String, String>();
						entry.put("number", String.valueOf(number)); // id
						entry.put("cardid",
								RandomStringUtils.randomAlphabetic(10));
						entry.put("playdate", "2014-01-10");
						entry.put("channel", "中华民国的国庆节是公元1911年10月10日");
						entry.put("program", content);
						products.add(entry);
					} else {
						// do nothing
					}
				}
			} else {
				// do nothing
			}
		}
		return products;
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
