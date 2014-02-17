package org.soul.elasticsearch.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.FieldNameAnalyzer;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.ParsedDocument;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
//import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;

public class MainSearchTest extends ElasticsearchIntegrationTest {
	private static Log log = LogFactory.getLog(MainSearchTest.class);

	@After
	public void afterTest() {
		// deletePluginsFolder();
	}

	@Before
	public void beforeTest() {
		// deletePluginsFolder();
	}

	DocumentMapperParser parser = new DocumentMapperParser(new Index("test"),
			newAnalysisService(), new PostingsFormatService(new Index("test")),
			newSimilarityLookupService());

	public static DocumentMapperParser newParser(Settings indexSettings) {
		return new DocumentMapperParser(new Index("test"), indexSettings,
				newAnalysisService(indexSettings), new PostingsFormatService(
						new Index("test")), newSimilarityLookupService());
	}

	MapperService newMapperService = new MapperService(new Index("test"),
			ImmutableSettings.Builder.EMPTY_SETTINGS, new Environment(),
			newAnalysisService(), null, new PostingsFormatService(new Index(
					"test")), newSimilarityLookupService());

	public static AnalysisService newAnalysisService() {
		return newAnalysisService(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	public static AnalysisService newAnalysisService(Settings indexSettings) {
		Injector parentInjector = new ModulesBuilder().add(
				new SettingsModule(indexSettings),
				new EnvironmentModule(new Environment(
						ImmutableSettings.Builder.EMPTY_SETTINGS)),
				new IndicesAnalysisModule()).createInjector();

		AnalysisModule analysisModule = new AnalysisModule(indexSettings,
				parentInjector.getInstance(IndicesAnalysisService.class));
		// analysisModule.addProcessor(new SoulAnalysisBinderProcessor());
		Injector injector = new ModulesBuilder().add(
				new IndexSettingsModule(new Index("test"), indexSettings),
				new IndexNameModule(new Index("test")), analysisModule)
				.createChildInjector(parentInjector);

		return injector.getInstance(AnalysisService.class);
	}

	public static SimilarityLookupService newSimilarityLookupService() {
		return new SimilarityLookupService(new Index("test"),
				ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	@Test
	public void testAnalyzerMapping() throws Exception {
		String mapping = XContentFactory.jsonBuilder().startObject()
				.startObject("type").startObject("_analyzer")
				.field("path", "field_analyzer").endObject()
				.startObject("properties").startObject("field1")
				.field("type", "string").endObject().startObject("field2")
				.field("type", "string").field("analyzer", "simple")
				.endObject().endObject().endObject().endObject().string();

		// analyzer的默认名字为_analyzer,现在修改了它的默认name为field_analyzer
		// 修改了field2的默认analyzer为simple

		log.info(mapping);

		DocumentMapper documentMapper = parser.parse(mapping);

		ParsedDocument doc = documentMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		FieldNameAnalyzer analyzer = (FieldNameAnalyzer) doc.analyzer();
		// doc的默认analyzer为whitespace了
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));

		// check that it serializes and de-serializes correctly
		log.info(documentMapper.mappingSource().string());
		DocumentMapper reparsedMapper = parser.parse(documentMapper
				.mappingSource().string());

		// doc = reparsedMapper.parse(
		// "type",
		// "1",
		// XContentFactory.jsonBuilder().startObject()
		// .field("field_analyzer", "whitespace")
		// .field("field1", "value1").field("field2", "value2")
		// .endObject().bytes());

		doc = reparsedMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));
	}

	@Test
	public void testAnalyzerMappingExplicit() throws Exception {
		String mapping = XContentFactory.jsonBuilder().startObject()
				.startObject("type").startObject("_analyzer")
				.field("path", "field_analyzer").endObject()
				.startObject("properties").startObject("field_analyzer")
				.field("type", "string").endObject().startObject("field1")
				.field("type", "string").endObject().startObject("field2")
				.field("type", "string").field("analyzer", "simple")
				.endObject().endObject().endObject().endObject().string();

		log.info(mapping);

		DocumentMapper documentMapper = parser.parse(mapping);

		ParsedDocument doc = documentMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field_analyzer", "whitespace")
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());
		log.info(doc.docs().get(0).getField("field_analyzer"));

		FieldNameAnalyzer analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));

		doc = documentMapper.parse(
				"type",
				"2",
				XContentFactory.jsonBuilder().startObject()
						.field("field_analyzer", "simple")
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		log.info(doc.docs().get(0).getField("field_analyzer"));
		analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("simple"));

		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));

		// check that it serializes and de-serializes correctly

		DocumentMapper reparsedMapper = parser.parse(documentMapper
				.mappingSource().string());

		doc = reparsedMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field_analyzer", "whitespace")
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));
	}

	@Test
	public void testAnalyzerMappingNotIndexedNorStored() throws Exception {
		String mapping = XContentFactory.jsonBuilder().startObject()
				.startObject("type").startObject("_analyzer")
				.field("path", "field_analyzer").endObject()
				.startObject("properties").startObject("field_analyzer")
				.field("type", "string").field("index", "no")
				.field("store", "no").endObject().startObject("field1")
				.field("type", "string").endObject().startObject("field2")
				.field("type", "string").field("analyzer", "simple")
				.endObject().endObject().endObject().endObject().string();

		DocumentMapper documentMapper = parser.parse(mapping);

		ParsedDocument doc = documentMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field_analyzer", "whitespace")
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		FieldNameAnalyzer analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));

		// check that it serializes and de-serializes correctly

		DocumentMapper reparsedMapper = parser.parse(documentMapper
				.mappingSource().string());

		doc = reparsedMapper.parse(
				"type",
				"1",
				XContentFactory.jsonBuilder().startObject()
						.field("field_analyzer", "whitespace")
						.field("field1", "value1").field("field2", "value2")
						.endObject().bytes());

		analyzer = (FieldNameAnalyzer) doc.analyzer();
		assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
				equalTo("whitespace"));
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));
	}

	@Test
	public void testMinScore() {
		createIndex("test");
		ensureGreen();

		client().prepareIndex("test", "test", "1").setSource("score", 1.5)
				.get();
		client().prepareIndex("test", "test", "2").setSource("score", 1).get();
		client().prepareIndex("test", "test", "3").setSource("score", 2).get();
		client().prepareIndex("test", "test", "4").setSource("score", 0.5)
				.get();
		refresh();

		SearchResponse searchResponse = client()
				.prepareSearch("test")
				.setQuery(
						functionScoreQuery(scriptFunction("_doc['score'].value")))
				.setMinScore(1.5f).get();
		assertHitCount(searchResponse, 2);
		assertFirstHit(searchResponse, hasId("3"));
		assertSecondHit(searchResponse, hasId("1"));
	}

	@Test
	public void testQueryStringWithSlopAndFields() {
		createIndex("test");
		ensureGreen();
		client().prepareIndex("test", "customer", "1")
				.setSource("desc", "one two three").get();
		client().prepareIndex("test", "product", "2")
				.setSource("desc", "one two three").execute().actionGet();

		// assertThat(
		// doc.get(docMapper.mappers().smartName("file1").mapper().names()
		// .indexName()), nullValue());
		refresh();
		{
			SearchResponse searchResponse = client()
					.prepareSearch("test")
					.setQuery(
							QueryBuilders.queryString("\"one two\"")
									.defaultField("desc")).get();
			assertHitCount(searchResponse, 2);
			log.info(searchResponse.getHits().getAt(1).getMatchedQueries());
		}
		{
			SearchResponse searchResponse = client()
					.prepareSearch("test")
					.setQuery(
							QueryBuilders.queryString("\"one two\"").field(
									"product.desc")).get();
			assertHitCount(searchResponse, 1);
		}
		{
			SearchResponse searchResponse = client()
					.prepareSearch("test")
					.setQuery(
							QueryBuilders.queryString("\"one three\"~5").field(
									"product.desc")).get();
			assertHitCount(searchResponse, 1);
		}
		{
			SearchResponse searchResponse = client()
					.prepareSearch("test")
					.setQuery(
							QueryBuilders.queryString("\"one three\"~3")
									.defaultField("customer.desc")).get();
			assertHitCount(searchResponse, 1);
		}
	}

	private static FilterBuilder rangeFilter(String field, Object from,
			Object to) {
		if (randomBoolean()) {
			if (randomBoolean()) {
				return FilterBuilders.rangeFilter(field).from(from).to(to);
			} else {
				return FilterBuilders.rangeFilter(field).from(from).to(to)
						.setExecution("fielddata");
			}
		} else {
			return FilterBuilders.numericRangeFilter(field).from(from).to(to);
		}
	}

	@Test
	public void testSimpleQueryString() {
		assertAcked(client().admin().indices().prepareCreate("test")
				.setSettings("index.number_of_shards", 1));
		client().prepareIndex("test", "type1", "1").setSource("body", "foo")
				.get();
		client().prepareIndex("test", "type1", "2").setSource("body", "bar")
				.get();
		client().prepareIndex("test", "type1", "3")
				.setSource("body", "foo bar").get();
		client().prepareIndex("test", "type1", "4")
				.setSource("body", "quux baz eggplant").get();
		client().prepareIndex("test", "type1", "5")
				.setSource("body", "quux baz spaghetti").get();
		client().prepareIndex("test", "type1", "7").setSource("body", "time")
				.get();
		client().prepareIndex("test", "type1", "6")
				.setSource("otherbody", "spaghetti").get();
		refresh();

		SearchResponse searchResponse = client().prepareSearch()
				.setQuery(simpleQueryString("foo bar")).get();
		assertHitCount(searchResponse, 3l);
		assertSearchHits(searchResponse, "1", "2", "3");

		searchResponse = client()
				.prepareSearch()
				.setQuery(
						simpleQueryString("foo bar").defaultOperator(
								SimpleQueryStringBuilder.Operator.AND)).get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("3"));

		searchResponse = client()
				.prepareSearch()
				.setQuery(
						simpleQueryString("\"quux baz\" +(eggplant | spaghetti)"))
				.get();
		assertHitCount(searchResponse, 2l);
		assertSearchHits(searchResponse, "4", "5");

		searchResponse = client().prepareSearch()
				.setQuery(simpleQueryString("eggplants").analyzer("snowball"))
				.get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("4"));

		searchResponse = client()
				.prepareSearch()
				.setQuery(
						simpleQueryString("spaghetti").field("body", 20.0f)
								.field("otherbody", 2.0f)).get();
		assertHitCount(searchResponse, 2l);
		assertFirstHit(searchResponse, hasId("5"));
		assertSearchHits(searchResponse, "5", "6");

		searchResponse = client().prepareSearch()
				.setQuery(simpleQueryString("timing").analyzer("snowball"))
				.get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("7"));
	}
}
