package org.lionsoul.jcseg.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.hasId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.app.SoulSearchClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
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
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SogouDataTest {
	private final Log log = LogFactory.getLog(SogouDataTest.class);
	private RestClient restClient;
	private Settings settings;
	TransportClient transportClient;
	SoulSearchClient searchClinet = new SoulSearchClient("localhost",
			"soul_mini", "table");
	private String indexName = "soul_mini";
	private String typeName = "table";
	// private String hostName = "192.168.50.75";
	private String hostName = "localhost";
	private String dirName = "/mnt/f/Sogou-mini/";
	private int port = 9300;

	@Before
	public void startNode() throws Exception {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "docno");
		String resource = indexName + "/" + typeName;
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
		// properties.put(ConfigurationOptions.ES_UPSERT_DOC, "false");
		properties.put("es.host", hostName);
		settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		restClient = new RestClient(settings);
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(hostName,
						port));
	}

	@After
	public void closeResources() {
		transportClient.close();
		restClient.close();
	}

	// @Test
	public void createIndexAndFillDataTest() {
		createIndexTestWithMapping();
		try {
			testIndexOperation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createIndexTestWithMapping() {
		try {
			IndicesExistsResponse existsResponse = transportClient.admin()
					.indices().prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				transportClient.admin().indices().prepareDelete(indexName)
						.execute().actionGet();
			}
			// .startObject("postTime").field("type", "date")
			// .field("format", "date_hour_minute_second")
			// .field("index", "not_analyzed").endObject()
			XContentBuilder builder = (XContentBuilder) jsonBuilder()
					.startObject().startObject("mappings")
					.startObject(typeName).startObject("properties")
					.startObject("url").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("postTime").field("type", "string")
					.field("index", "not_analyzed").endObject()
					.startObject("contenttitle").field("type", "string")
					.field("index_analyzer", "soul_pinyin")
					.field("search_analyzer", "soul_query").endObject()
					.startObject("content").field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("search_analyzer", "soul_query").endObject()
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

	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		SogouDataReader reader = new SogouDataReader(dirName);
		List<HashMap<String, String>> result = null;
		BytesArray data = new BytesArray(4096 * 1024);
		while ((result = reader.next()) != null) {
			log.info(result.size());
			for (int i = 0; i < result.size(); i++) {
				Map<String, String> entry = result.get(i);
				MapWritable writable = (MapWritable) WritableUtils
						.toWritable(entry);
				int entrySize = command.prepare(writable);
				log.info(entrySize + "," + data.size() + "," + data.capacity());
				if (entrySize + data.size() > data.capacity()) {
					restClient.bulk(settings.getIndexType(), data.bytes(),
							data.size());
					data.reset();
				}
				command.write(writable, data);
			}
		}
		restClient.bulk(settings.getIndexType(), data.bytes(), data.size());
	}
	// @Test
	public void testSimpleQueryStringOperation() {
		// 使用soul_query分完词后，建立boolean查询，此时与顺序无关
		// String queryStrs[] = {"Google雅虎", "Google雅虎责任编辑", "互联网Google",
		// "雅虎北京",
		// "奥斯卡 艺术"};
		String queryStrs[] = {"Google雅虎"};
		for (String queryStr : queryStrs) {
			int size = 0;
			long totalSize = 0;
			SimpleQueryStringBuilder builder = simpleQueryString(queryStr)
					.analyzer("soul_query").field("content", 1.0f)
					.field("contenttitle", 2.0f)
					.defaultOperator(SimpleQueryStringBuilder.Operator.AND);
			SearchRequestBuilder srb = transportClient.prepareSearch(indexName)
					.setQuery(builder).addHighlightedField("contenttitle")
					.addHighlightedField("content");
			SearchResponse searchResponse = null;
			log.info("******************* " + queryStr + " *******************");
			do {
				srb.setFrom(size);
				searchResponse = srb.execute().actionGet();
				totalSize = searchResponse.getHits().getTotalHits();
				size += searchResponse.getHits().getHits().length;
				for (SearchHit hit : searchResponse.getHits().getHits()) {
					log.info(hit.getHighlightFields().get("contenttitle"));
					log.info(hit.getHighlightFields().get("content"));
				}
			} while (size < totalSize);
			log.info("******************* " + queryStr + "/" + totalSize
					+ " *******************");
		}
	}

	// @Test
	public void testSimpleScrollQueryThenFetch() throws Exception {
		String queryStrs[] = {"Google雅虎", "Google雅虎责任编辑", "互联网Google", "雅虎北京",
				"奥斯卡 艺术"};
		for (String queryStr : queryStrs) {
			log.info("******************* " + queryStr + " *******************");
			SimpleQueryStringBuilder strBuilder = simpleQueryString(queryStr)
					.analyzer("soul_query").field("content", 1.0f)
					.field("contenttitle", 2.0f)
					.defaultOperator(SimpleQueryStringBuilder.Operator.AND);
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName).setQuery(strBuilder).setSize(15)
					.setScroll(TimeValue.timeValueMinutes(4)).execute()
					.actionGet();
			for (SearchHit hit : searchResponse.getHits().getHits()) {
				// log.info(hit.getId() + ", " + hit.getScore() + ", "
				// + hit.getSource().get("url") + ", "
				// + hit.getSource().get("contenttitle"));
			}
			int size = searchResponse.getHits().getHits().length;
			long totalSize = searchResponse.getHits().getTotalHits();
			while (size < totalSize) {
				searchResponse = transportClient
						.prepareSearchScroll(searchResponse.getScrollId())
						.setScroll(TimeValue.timeValueMinutes(4)).execute()
						.actionGet();
				for (SearchHit hit : searchResponse.getHits().getHits()) {
					// log.info(hit.getId() + ", " + hit.getScore() + ", "
					// + hit.getSource().get("url") + ", "
					// + hit.getSource().get("contenttitle"));
				}
				size += searchResponse.getHits().getHits().length;
			};
			log.info("******************* " + queryStr + "/" + totalSize
					+ " *******************");
		}
	}

	// @Test
	public void testMatchAllQuery() throws Exception {
		SearchResponse searchResponse = transportClient
				.prepareSearch(indexName).setQuery(matchAllQuery()).setSize(30)
				.setScroll(TimeValue.timeValueMinutes(2)).execute().actionGet();
		assertThat(searchResponse.getHits().hits().length, equalTo(30));
		for (SearchHit hit : searchResponse.getHits()) {
			// log.info(hit.getSource().get("contenttitle") + "  "
			// + hit.getSource().get("postTime"));
		}
		for (int i = 0; i < 4; i++) {
			searchResponse = transportClient
					.prepareSearchScroll(searchResponse.getScrollId())
					.setScroll(TimeValue.timeValueMinutes(2)).execute()
					.actionGet();
			assertThat(searchResponse.getHits().hits().length, equalTo(30));
			for (SearchHit hit : searchResponse.getHits()) {
				// log.info(hit.getSource().get("contenttitle") + "  "
				// + hit.getSource().get("postTime"));
			}
		}
	}
	@Test
	public void testSimpleScrollQueryWithHighLight() throws Exception {
		// String queryStrs[] = {"互联网Google", "雅虎北京", "奥斯卡 艺术"};
		String queryStrs[] = {"雅虎Google中国"};
		for (String queryStr : queryStrs) {
			// searchClinet.simpleScrollQuery(queryStr);
			searchClinet.multiMatchQuery(queryStr);
		}
	}
}
