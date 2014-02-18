package org.soul.elasticsearch.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

public class ElasticSearchMappingTest extends ElasticsearchIntegrationTest {
	private static Log log = LogFactory.getLog(ElasticSearchMappingTest.class);
	public static SimilarityLookupService newSimilarityLookupService() {
		return new SimilarityLookupService(new Index("test"),
				ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	public static AnalysisService newAnalysisService() {
		return newAnalysisService(ImmutableSettings.Builder.EMPTY_SETTINGS);
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

		DocumentMapper docMapper = parser.parse(mapping);
		ParsedDocument doc = docMapper.parse(
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

		DocumentMapper reparsedMapper = parser.parse(docMapper.mappingSource()
				.string());

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
		// assertThat(((NamedAnalyzer) analyzer.defaultAnalyzer()).name(),
		// equalTo("whitespace"));
		log.info(((NamedAnalyzer) analyzer.defaultAnalyzer()).name());
		// assertThat((NamedAnalyzer) analyzer.defaultAnalyzer(), nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field1")),
				nullValue());
		assertThat(((NamedAnalyzer) analyzer.analyzers().get("field2")).name(),
				equalTo("simple"));

		// check that it serializes and de-serializes correctly
		log.info(documentMapper.mappingSource().string());
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
}
