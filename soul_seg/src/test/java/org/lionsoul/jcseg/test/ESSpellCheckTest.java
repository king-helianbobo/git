package org.lionsoul.jcseg.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.*;
import java.util.*;

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

public class ESSpellCheckTest {
	private final Log log = LogFactory.getLog(ESSpellCheckTest.class);
	private RestClient client;
	private Settings settings;
	TransportClient transportClient;
	private String indexName = "spellcheck";
	private String typeName = "table";
	// private String hostName = "192.168.50.75";
	private String hostName = "localhost";
	private int port = 9300;

	@Before
	public void startNode() throws Exception {
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(hostName,
						port));
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "number");
		properties.put(ConfigurationOptions.ES_RESOURCE, indexName + "/"
				+ typeName);
		// properties.put(ConfigurationOptions.ES_UPSERT_DOC, "false");
		properties.put("es.host", hostName);
		settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		client = new RestClient(settings);
	}
	 @Ignore("Create Mapping")
	@Test
	public void createIndexMappingTest() {
		try {
			IndicesExistsResponse existsResponse = transportClient.admin()
					.indices().prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				transportClient.admin().indices().prepareDelete(indexName)
						.execute().actionGet();
			}

			XContentBuilder builder = (XContentBuilder) jsonBuilder()
					.startObject().startObject("settings")
					.startObject("analysis").startObject("analyzer")
					.startObject("default").field("type", "whitespace")
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
	@After
	public void closeResources() {
		transportClient.close();
		client.close();
	}

//	@Ignore("Index Operation")
	@Test
	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		InputStream in = new FileInputStream("/mnt/f/tmp/Sogou.dic");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				"UTF-8"));
		final int size = 100;
		int startNumber = 0;
		List<String> result;
		BytesArray data = new BytesArray(160 * 1024);
		while ((result = SogouDataReader.readOneLine(reader, size)) != null) {
			for (int i = 0; i < result.size(); i++) {
				Map<String, String> entry = new HashMap<String, String>();
				entry.put("number", String.valueOf(startNumber++));
				entry.put("content", result.get(i));
				MapWritable wr = (MapWritable) WritableUtils.toWritable(entry);
				int entrySize = command.prepare(wr);
				log.info(entrySize + "," + data.size() + "," + data.capacity());
				if (entrySize + data.size() > data.capacity()) {
					client.bulk(settings.getIndexType(), data.bytes(),
							data.size());
					data.reset();
				}
				command.write(wr, data);
			}
		}
		client.bulk(settings.getIndexType(), data.bytes(), data.size());
	}

}
