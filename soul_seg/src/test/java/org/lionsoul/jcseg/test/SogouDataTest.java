package org.lionsoul.jcseg.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SogouDataTest {
	private final Log log = LogFactory.getLog(SogouDataTest.class);
	private RestClient restClient;
	private Settings settings;
	TransportClient transportClient;

	// private String indexName = "sogou_test";
	// private String typeName = "test1";
	private String indexName = "sogou_mini";
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
		// DeleteIndexResponse deleteIndexResponse = transportClient.admin()
		// .indices().prepareDelete(indexName).execute().actionGet();
		// assertThat(deleteIndexResponse.isAcknowledged(), is(true));
		transportClient.close();
		restClient.close();
	}

	@Ignore("Create Index sogou_test")
	@Test
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

//	@Ignore("Send Data to Index Module")
	@Test
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

}
