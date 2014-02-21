package org.lionsoul.elasticsearch.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.MapWritable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Lists;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.plugin.SoulPinyinAnalyzer;
import org.testng.annotations.*;

public class PinyinTokenFilterTest {

	private Log log = LogFactory.getLog(PinyinTokenFilterTest.class);

	TokenStream result = null;
	private RestClient client;
	private Settings settings;
	TransportClient transportClient;
	private String indexName = "pinyin_test";
	private String typeName = "test1";
	private String hostName = "localhost";
	private int port = 9300;

	@BeforeClass
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

	@AfterClass
	public void closeResources() {
		// DeleteIndexResponse deleteIndexResponse = transportClient.admin()
		// .indices().prepareDelete(indexName).execute().actionGet();
		// assertThat(deleteIndexResponse.isAcknowledged(), is(true));
		transportClient.close();
		client.close();
	}

	// @Test(enabled = true)

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

	@Test(enabled = false)
	public void testMethod1() throws Exception {
		createIndexTestWithMapping();
		testIndexOperation();
	}

	@Test(enabled = true)
	public void testMethod2() throws Exception {
		createIndexTestWithMapping2();
		testIndexOperation2();
	}

	private void createIndexTestWithMapping2() {
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
					.field("index_analyzer", "soul_pinyin")
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

	public void testIndexOperation2() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		InputStream in = this.getClass().getResourceAsStream("/content2.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,
				"UTF-8"));
		List<Map<String, String>> result = SogouDataReader.getTestData(reader);
		BytesArray data = new BytesArray(64 * 1024);
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

	// @Test(enabled = true)
	public void testIndexOperation() throws Exception {
		IndexCommand command = new IndexCommand(settings);
		SogouDataReader reader = new SogouDataReader("/mnt/f/Sogou-mini/");
		List<HashMap<String, String>> result = null;
		BytesArray data = new BytesArray(1024 * 1024);
		while ((result = reader.next()) != null) {
			log.info(result.size());
			for (int i = 0; i < result.size(); i++) {
				Map<String, String> entry = result.get(i);
				MapWritable writable = (MapWritable) WritableUtils
						.toWritable(entry);
				int entrySize = command.prepare(writable);
				log.info(entrySize + "," + data.size() + "," + data.capacity());
				if (entrySize + data.size() > data.capacity()) {
					client.bulk(settings.getIndexType(), data.bytes(),
							data.size());
					data.reset();
				}
				command.write(writable, data);
			}
		}
		client.bulk(settings.getIndexType(), data.bytes(), data.size());
	}

	// @Test
	public void testSimpleQueryStringOperation() {
		// 使用soul_query分完词后，建立boolean查询，此时与顺序无关
		String queryStrs[] = {"深邃", "zuim", "浅易", "高深"};
		for (String queryStr : queryStrs) {
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName)
					.setQuery(
							simpleQueryString(queryStr)
									.analyzer("soul_query")
									.field("title")
									.defaultOperator(
											SimpleQueryStringBuilder.Operator.AND))
					.get();

			log.info("SimpleQueryStringTest: [" + queryStr + "] ***********"
					+ searchResponse.toString());
		}
	}

	// @Test
	public void testQueryStringOperation() {
		// 词组后面跟随~10,表示词组中的多个词之间的距离之和不超过10,则满足查询
		// 词之间的距离,即查询词组中词为满足和目标词组相同的最小移动次数
		String queryStrs[] = {"深邃", "zuim", "浅易", "高深"};
		for (String str : queryStrs) {
			SearchResponse searchResponse = transportClient
					.prepareSearch(indexName)
					.setQuery(
							QueryBuilders
									.queryString(str)
									.defaultField("title")
									.defaultOperator(
											QueryStringQueryBuilder.Operator.AND))
					.get();
			log.info("QueryStringTest[" + str + "] ***********"
					+ searchResponse.toString());
		}
	}

	// @Test
	public void TestPinyinAnalyzer() {
		String[] titles = {"台湾最美胸部再次自拍   深邃乳沟", "哆啦Ａ梦玩ＣＯＳ穿上“马甲”爆笑造型", "范冰冰",
				"公务员面试名单：４１９江西省气象局", "图文：王北星获速滑１０００米冠军　一脸笑容", "服装：花样长毛衣搭配出新感觉",
				"《长江七号》英文主题曲曝光　温暖而怀旧（图）", "郎朗出任万宝龙文化基金会主席",
				"超级病菌导致全球两栖动物面临灭绝（组图）", "服装：明星支招教你如何穿皮衣",
				"微软并购雅虎传闻愈演愈烈　杨致远可能走人", "南粤风采２６选５电脑福利彩票第８７７期开奖公告",
				"奥运网络安全应急小组成立", "广西受灾人数升至千万", "泰国热播电视剧惹怒空乘人员",
				"《龙珠》墨西哥热拍　周润发抱怨饮食太差（图）", "中航地产大股东拟１２．４亿增持近２８％股权",
				"北京奥运临近冲突连连　手球联合会表态重罚亚洲"};
		Analyzer analyzer = new SoulPinyinAnalyzer();
		for (String title : titles) {
			analyze(analyzer, title);
		}
	}

	void analyze(Analyzer analyzer, String text) {
		try {
			TokenStream tokenStream = analyzer.tokenStream("content",
					new StringReader(text));
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				CharTermAttribute charAttribute = tokenStream
						.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttribute = tokenStream
						.getAttribute(OffsetAttribute.class);
				TypeAttribute typeAtt = tokenStream
						.getAttribute(TypeAttribute.class);
				PositionIncrementAttribute positionAttr = tokenStream
						.getAttribute(PositionIncrementAttribute.class);

				log.info("Token Stream is [" + offsetAttribute.startOffset()
						+ "," + offsetAttribute.endOffset() + ","
						+ charAttribute.toString() + "," + typeAtt.type() + ","
						+ positionAttr.getPositionIncrement() + "]");
			}
			tokenStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
