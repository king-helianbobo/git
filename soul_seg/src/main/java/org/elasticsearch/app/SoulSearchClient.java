package org.elasticsearch.app;

import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.cfg.SettingsManager;
import org.elasticsearch.hadoop.mr.MapReduceWriter;
import org.elasticsearch.hadoop.rest.InitializationUtils;
import org.elasticsearch.hadoop.rest.RestClient;
import org.elasticsearch.hadoop.serialization.MapWritableIdExtractor;
import org.elasticsearch.hadoop.serialization.SerializationUtils;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;

public class SoulSearchClient {
	private final Log log = LogFactory.getLog(SoulSearchClient.class);
	private RestClient restClient;
	private Settings settings;
	TransportClient transportClient;
	private String indexName = "soul_mini";
	private String typeName = "table";
	private String hostName = "localhost";
	private int port = 9300;

	public SoulSearchClient() {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "docno");
		String resource = indexName + "/" + typeName;
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
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

	public void close() {
		transportClient.close();
		restClient.close();
	}

	public String simpleQueryStringQuery(String queryStr) {
		// 使用soul_query分完词后，建立boolean查询，此时与顺序无关
		SearchResponse searchResponse = transportClient
				.prepareSearch(indexName)
				.setQuery(
						simpleQueryString(queryStr)
								.analyzer("soul_query")
								.field("content", 1.0f)
								.field("contenttitle", 2.0f)
								.defaultOperator(
										SimpleQueryStringBuilder.Operator.AND))
				.get();
		log.info("******************* " + queryStr + " *******************");
		log.info(searchResponse.getHits().getHits().length);
		StringBuilder builder = new StringBuilder();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			String str = hit.getId() + ", " + hit.getScore() + ", "
					+ hit.getSource().get("url") + ", "
					+ hit.getSource().get("contenttitle");
			builder.append(str + "\n");
			log.info(str);
		}
		log.info("******************* " + queryStr + " *******************");
		return builder.toString();
	}
}
