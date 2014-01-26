package org.lionsoul.jcseg.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EsSpellCheckTest {
	private final Log log = LogFactory.getLog(EsSpellCheckTest.class);
	private RestClient client;
	private Settings settings;
	TransportClient transportClient;
	private String indexName = "sogou_spellcheck";
	private String typeName = "table";
	//private String hostName = "192.168.50.75";
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

	@After
	public void closeResources() {
		transportClient.close();
		client.close();
	}

	// @Ignore("Index Operation")
	@Test
	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		InputStream in = new FileInputStream("/mnt/f/tmp/Sogou.dic");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				"UTF-8"));
		final int size = 100;
		int startNumber = 0;
		List<String> result;
		BytesArray data = new BytesArray(16 * 1024);
		while ((result = getTestData(reader, size)) != null) {
			for (int i = 0; i < result.size(); i++) {
				Map<String, String> entry = new HashMap<String, String>();
				entry.put("content", result.get(i));
				entry.put("number", String.valueOf(startNumber++));
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
	@SuppressWarnings("unchecked")
	private List<String> getTestData(BufferedReader reader, int size)
			throws IOException {
		String temp = null;
		List<String> entry = new LinkedList<String>();
		int n = 0;
		while ((temp = reader.readLine()) != null) {
			temp = temp.trim();
			entry.add(temp);
			n++;
			if (n >= size)
				break;
		}
		if (entry.size() > 0)
			return entry;
		else
			return null;
	}

}
