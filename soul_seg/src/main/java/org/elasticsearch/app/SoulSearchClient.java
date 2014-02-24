package org.elasticsearch.app;

import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.cfg.SettingsManager;
import org.elasticsearch.hadoop.mr.MapReduceWriter;
import org.elasticsearch.hadoop.rest.InitializationUtils;
import org.elasticsearch.hadoop.rest.RestClient;
import org.elasticsearch.hadoop.serialization.MapWritableIdExtractor;
import org.elasticsearch.hadoop.serialization.SerializationUtils;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.suggest.elasticsearch.action.suggest.SuggestResponse;
import org.suggest.elasticsearch.client.action.SuggestRequestBuilder;

public class SoulSearchClient {
	private final Log log = LogFactory.getLog(SoulSearchClient.class);
	private RestClient restClient;
	TransportClient transportClient;
	private String indexName = "soul_mini";
	private String typeName = "table";
	private String hostName = "localhost";
	private int port = 9300;

	public SoulSearchClient() {
		restClient = _GetRestClient();
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(hostName,
						port));
	}

	private RestClient _GetRestClient() {
		Properties properties = new Properties();
		properties.put(ConfigurationOptions.ES_WRITE_OPERATION, "index");
		properties.put(ConfigurationOptions.ES_MAPPING_ID, "docno");
		String resource = this.indexName + "/" + this.typeName;
		properties.put(ConfigurationOptions.ES_RESOURCE, resource);
		properties.put("es.host", hostName);
		Settings settings = SettingsManager.loadFrom(properties);
		SerializationUtils.setValueWriterIfNotSet(settings,
				MapReduceWriter.class, log);
		InitializationUtils.setIdExtractorIfNotSet(settings,
				MapWritableIdExtractor.class, log);
		restClient = new RestClient(settings);
		return restClient;
	}

	public SoulSearchClient(String hostName, String indexName, String typeName) {
		this.hostName = hostName;
		this.indexName = indexName;
		this.typeName = typeName;
		restClient = _GetRestClient();
		transportClient = new TransportClient()
				.addTransportAddress(new InetSocketTransportAddress(hostName,
						port));
	}

	public void close() {
		transportClient.close();
		restClient.close();
	}

	public void simpleScrollQuery(String queryStr) {
		String field = "contenttitle";
		SimpleQueryStringBuilder strBuilder = simpleQueryString(queryStr)
				.analyzer("soul_query").field("content", 1.0f)
				.field(field, 2.0f)
				.defaultOperator(SimpleQueryStringBuilder.Operator.AND);
		SearchResponse searchResponse = null;
		int size = 0;
		long totalSize = 0;
		do {
			if (searchResponse == null)
				searchResponse = transportClient.prepareSearch(indexName)
						.setQuery(strBuilder).setSize(7)
						.setScroll(TimeValue.timeValueMinutes(4))
						.addHighlightedField(field)
						.addHighlightedField("content").execute().actionGet();
			else {
				searchResponse = transportClient
						.prepareSearchScroll(searchResponse.getScrollId())
						.setScroll(TimeValue.timeValueMinutes(4)).execute()
						.actionGet();
			}
			size += searchResponse.getHits().getHits().length;
			totalSize = searchResponse.getHits().getTotalHits();
			// log.info(searchResponse.getScrollId());
			log.info(size + "," + totalSize);
			for (SearchHit hit : searchResponse.getHits().getHits()) {
				if ((hit.getHighlightFields().get(field) != null)
						&& (hit.getHighlightFields().get("content") != null)) {
					log.info("\n{\n"
							+ hit.getHighlightFields().get(field).fragments()[0]
							+ "\n"
							+ hit.getHighlightFields().get("content")
									.fragments()[0] + "\n}");
				} else if (hit.getHighlightFields().get("content") != null)
					log.info(hit.getHighlightFields().get("content")
							.fragments()[0]);
				else if (hit.getHighlightFields().get(field) != null)
					log.info(hit.getHighlightFields().get(field).fragments()[0]);
				else {
					log.error("This should not happen!");
				}
			}
		} while (size < totalSize);
	}
	public String simpleTermQuery(String queryStr) {
		String field = "contenttitle";
		SearchResponse searchResponse = transportClient
				.prepareSearch(indexName).setQuery(termQuery(field, queryStr))
				.addHighlightedField(field).get();
		log.info("******************* " + queryStr + " *******************");
		StringBuilder builder = new StringBuilder();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			String str = hit.getSource().get(field) + "\n";
			builder.append(str + "\n");
			log.info(str);
		}
		return builder.toString();
	}
	public String synonymQuery(String queryStr) {
		String field = "contenttitle";
		SearchResponse searchResponse = transportClient
				.prepareSearch(indexName)
				.setQuery(
						simpleQueryString(queryStr)
								.analyzer("soul_query")
								.field(field, 1.0f)
								.defaultOperator(
										SimpleQueryStringBuilder.Operator.OR))
				.addHighlightedField(field).get();
		log.info("******************* " + queryStr + " *******************");
		log.info(searchResponse.getHits().getHits().length);
		StringBuilder builder = new StringBuilder();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			String str = hit.getId() + ", " + hit.getScore() + ", "
					+ hit.getSource().get("url") + ", "
					+ hit.getSource().get(field) + ", "
					+ hit.getHighlightFields().get(field).fragments()[0];
			builder.append(str + "\n");
			log.info(str);
		}
		log.info("******************* " + queryStr + " *******************");
		return builder.toString();
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

	private void logInfo(SearchHit hit, String field1, String field2) {
		log.info("分数： " + hit.getScore());
		StringBuilder builder = new StringBuilder();
		if ((hit.getHighlightFields().get(field1) != null)
				&& (hit.getHighlightFields().get(field2) != null)) {
			int frag1 = hit.getHighlightFields().get(field1).fragments().length;
			int frag2 = hit.getHighlightFields().get(field2).fragments().length;
			log.info("frag1=  " + frag1 + ", frag2 = " + frag2);
			builder.append("\n{\n");
			for (int i = 0; i < frag1; i++) {
				builder.append(hit.getHighlightFields().get(field1).fragments()[i]);
			}
			builder.append("\n");
			for (int i = 0; i < frag2; i++) {
				builder.append(hit.getHighlightFields().get(field2).fragments()[i]);
			}
			builder.append("\n}");
		} else if (hit.getHighlightFields().get(field2) != null) {
			int frag2 = hit.getHighlightFields().get(field2).fragments().length;
			builder.append("\n{\n");
			for (int i = 0; i < frag2; i++) {
				builder.append(hit.getHighlightFields().get(field2).fragments()[i]);
			}
			builder.append("\n}");
		}

		else if (hit.getHighlightFields().get(field1) != null) {
			int frag1 = hit.getHighlightFields().get(field1).fragments().length;
			builder.append("\n{\n");
			for (int i = 0; i < frag1; i++) {
				builder.append(hit.getHighlightFields().get(field1).fragments()[i]);
			}
			builder.append("\n}");
		} else {
			log.error("This should not happen!");
		}
		log.info(builder.toString());
	}

	public void multiMatchQuery(String queryStr) {
		MultiMatchQueryBuilder multiMatchQuery = new MultiMatchQueryBuilder(
				queryStr, "contenttitle", "content").analyzer("soul_query")
				.operator(MatchQueryBuilder.Operator.OR);
		multiMatchQuery.useDisMax(true);
		multiMatchQuery.minimumShouldMatch("10%");
		SearchResponse searchResponse = transportClient
				.prepareSearch(indexName).setQuery(multiMatchQuery)
				.addHighlightedField("contenttitle")
				.addHighlightedField("content").get();

		for (SearchHit hit : searchResponse.getHits().getHits()) {
			logInfo(hit, "contenttitle", "content");
		}
	}

	public List<String> getSuggestions(String inputStr) throws Exception {
		// SuggestRequestBuilder builder = new SuggestRequestBuilder(
		// transportClient).setIndices("sogou_spellcheck")
		// .field("content").term(inputStr).size(10).similarity(0.7f)
		// .suggestType("soul");
		// builder.preservePositionIncrements(true);
		// SuggestResponse suggestResponse = builder.execute().actionGet();
		// // assertThat(suggestResponse.getShardFailures(), is(emptyArray()));
		// log.info(suggestResponse.suggestions());
		// return suggestResponse.suggestions();
		List<String> resultStr = new LinkedList<String>();
		for (int i = 0; i < 4; i++) {
			String str = inputStr + RandomStringUtils.randomAlphabetic(5);
			resultStr.add(str);
		}
		log.info(resultStr);
		return resultStr;
	}
}
