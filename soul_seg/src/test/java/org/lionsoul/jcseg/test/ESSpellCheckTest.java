package org.lionsoul.jcseg.test;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
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
	private String indexName = "sogou_spellcheck";
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
