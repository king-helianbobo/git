package org.lionsoul.elasticsearch.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.app.SoulTransportClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SynonymTest {
	TransportClient client;
	private final Log log = LogFactory.getLog(SynonymTest.class);
	private String indexName = "synonym-test";
	private String typeName = "test1";
	private int port = 9300;

	SoulTransportClient searchClinet = new SoulTransportClient("localhost",
			"soul_mini", "table");

	@Before
	public void startClient() throws Exception {
		client = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", port));
	}
	@After
	public void closeClient() {
		client.close();
	}
	@Ignore
	@Test
	public void createIndexTestWithMapping() {
		try {
			IndicesExistsResponse existsResponse = client.admin().indices()
					.prepareExists(indexName).execute().actionGet();
			if (existsResponse.isExists()) { // if index exist, delete it
				client.admin().indices().prepareDelete(indexName).execute()
						.actionGet();
			}
			XContentBuilder builder1 = (XContentBuilder) jsonBuilder()
					.startObject().startObject("analysis")
					.startObject("analyzer").startObject("synonym")
					.field("tokenizer", "whitespace").startArray("filter")
					.value("synonym").endArray().endObject().endObject()
					.startObject("filter").startObject("synonym")
					.field("type", "file_watcher_synonym")
					.field("synonyms_path", "library/synonym.txt")
					.field("interval", "2s").endObject().endObject()
					.endObject();
			String mappings = builder1.string();
			log.info(mappings);

			XContentBuilder builder2 = (XContentBuilder) jsonBuilder()
					.startObject().startObject(typeName)
					.startObject("properties").startObject("content")
					.field("type", "string")
					.field("index_analyzer", "soul_index")
					.field("search_analyzer", "synonym").endObject()
					.endObject().endObject().endObject();

			log.info(builder2.string());

			CreateIndexResponse createIndexResponse = client.admin().indices()
					.prepareCreate(indexName).setSettings(builder1)
					.addMapping(typeName, builder2).execute().actionGet();
			assertThat(createIndexResponse.isAcknowledged(), is(true));
			client.admin().cluster().prepareHealth(indexName)
					.setWaitForGreenStatus().execute().actionGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void synonymQueryTest() {
		String[] queryStrs = {"面试过程", "公积", "口试口水", "口试 guangd", "guang Dong"};
		for (String queryStr : queryStrs) {
			searchClinet.synonymQuery(queryStr);
		}
	}
}
