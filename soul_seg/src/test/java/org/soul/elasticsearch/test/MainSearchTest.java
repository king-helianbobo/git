package org.soul.elasticsearch.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
//import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class MainSearchTest extends ElasticsearchIntegrationTest {
	private static Log log = LogFactory.getLog(MainSearchTest.class);

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

	@Test
	public void testMultiMatchQuery() throws Exception {
		assertAcked(client().admin().indices().prepareCreate("test")
				.setSettings(SETTING_NUMBER_OF_SHARDS, 1));

		indexRandom(
				true,
				client().prepareIndex("test", "type1", "1").setSource("field1",
						"value1", "field2", "value4", "field3", "value3"),
				client().prepareIndex("test", "type1", "2").setSource("field1",
						"value2", "field2", "value5", "field3", "value2"),
				client().prepareIndex("test", "type1", "3").setSource("field1",
						"value3", "field2", "value6", "field3", "value1"));

		MultiMatchQueryBuilder builder = multiMatchQuery(
				"value1 value2 value4", "field1", "field2");
		SearchResponse searchResponse = client().prepareSearch()
				.setQuery(builder)
				.addFacet(FacetBuilders.termsFacet("field1").field("field1"))
				.get();

		assertHitCount(searchResponse, 2l);
		// this uses dismax so scores are equal and the order can be arbitrary
		assertSearchHits(searchResponse, "1", "2");

		builder.useDisMax(false);
		searchResponse = client().prepareSearch().setQuery(builder).get();

		assertHitCount(searchResponse, 2l);
		assertSearchHits(searchResponse, "1", "2");

		client().admin().indices().prepareRefresh("test").get();
		builder = multiMatchQuery("value1", "field1", "field2").operator(
				MatchQueryBuilder.Operator.AND);
		// Operator only applies on terms inside a field!
		// Fields are always OR-ed together.
		searchResponse = client().prepareSearch().setQuery(builder).get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("1"));

		refresh();
		builder = multiMatchQuery("value1", "field1", "field3^1.5").operator(
				MatchQueryBuilder.Operator.AND);
		// Operator only applies on terms inside a field!
		// Fields are always OR-ed together.
		searchResponse = client().prepareSearch().setQuery(builder).get();
		assertHitCount(searchResponse, 2l);
		assertSearchHits(searchResponse, "3", "1");

		client().admin().indices().prepareRefresh("test").get();
		builder = multiMatchQuery("value1").field("field1")
				.field("field3", 1.5f).operator(MatchQueryBuilder.Operator.AND);
		// Operator only applies on terms inside a field!
		// Fields are always OR-ed together
		searchResponse = client().prepareSearch().setQuery(builder).get();
		assertHitCount(searchResponse, 2l);
		assertSearchHits(searchResponse, "3", "1");

		// Test lenient
		client().prepareIndex("test", "type1", "3")
				.setSource("field1", "value7", "field2", "value8", "field4", 5)
				.get();
		refresh();

		builder = multiMatchQuery("value1", "field1", "field2", "field4");
		try {
			client().prepareSearch().setQuery(builder).get();
			fail("Exception expected");
		} catch (SearchPhaseExecutionException e) {
			assertThat(e.shardFailures()[0].status(),
					equalTo(RestStatus.BAD_REQUEST));
		}
		builder.lenient(true);
		searchResponse = client().prepareSearch().setQuery(builder).get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("1"));
	}

	@Test
	public void testMatchQueryZeroTermsQuery() {
		assertAcked(client().admin().indices().prepareCreate("test")
				.setSettings(SETTING_NUMBER_OF_SHARDS, 1));
		client().prepareIndex("test", "type1", "1")
				.setSource("field1", "value1").get();
		client().prepareIndex("test", "type1", "2")
				.setSource("field1", "value2").get();
		refresh();

		BoolQueryBuilder boolQuery = boolQuery().must(
				matchQuery("field1", "a").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE)).must(
				matchQuery("field1", "value1").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE));
		SearchResponse searchResponse = client().prepareSearch()
				.setQuery(boolQuery).get();
		assertHitCount(searchResponse, 0l);
		// ZeroTermsQuery.NONE是一种filter，禁止空文档通过
		// ZeroTermsQuery.ALL也是一种filter，即允许任何文档通过

		boolQuery = boolQuery().must(
				matchQuery("field1", "value1").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 1l);

		boolQuery = boolQuery().must(
				matchQuery("field1", "a").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL)).must(
				matchQuery("field1", "value1").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 1l);

		boolQuery = boolQuery().must(
				matchQuery("field1", "a").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 2l);
	}
	@Test
	public void testMultiMatchQueryZeroTermsQuery() {
		assertAcked(client().admin().indices().prepareCreate("test")
				.setSettings(SETTING_NUMBER_OF_SHARDS, 1));
		client().prepareIndex("test", "type1", "1")
				.setSource("field1", "value1", "field2", "value2").get();
		client().prepareIndex("test", "type1", "2")
				.setSource("field1", "value3", "field2", "value4").get();
		refresh();

		BoolQueryBuilder boolQuery = boolQuery().must(
				multiMatchQuery("a", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE)).must(
				multiMatchQuery("value1", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE));
		// Fields are ORed together
		SearchResponse searchResponse = client().prepareSearch()
				.setQuery(boolQuery).get();
		assertHitCount(searchResponse, 0l);

		boolQuery = boolQuery().must(
				multiMatchQuery("a", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL)).must(
				multiMatchQuery("value4", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 1l);

		boolQuery = boolQuery().must(
				multiMatchQuery("value4", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 1l);

		boolQuery = boolQuery().must(
				multiMatchQuery("a", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.ALL));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 2l);

		boolQuery = boolQuery().must(
				multiMatchQuery("a", "field1", "field2").zeroTermsQuery(
						MatchQueryBuilder.ZeroTermsQuery.NONE));
		searchResponse = client().prepareSearch().setQuery(boolQuery).get();
		assertHitCount(searchResponse, 0l);
	}

	@Test
	public void testMultiMatchQueryMinShouldMatch() {
		assertAcked(client().admin().indices().prepareCreate("test")
				.setSettings(SETTING_NUMBER_OF_SHARDS, 1));
		client().prepareIndex("test", "type1", "1")
				.setSource("field1", new String[]{"value1", "value2", "value3"})
				.get();
		client().prepareIndex("test", "type1", "2")
				.setSource("field2", "value1").get();
		refresh();

		MultiMatchQueryBuilder multiMatchQuery = multiMatchQuery(
				"value1 value2 foo", "field1", "field2");

		multiMatchQuery.useDisMax(true);
		multiMatchQuery.minimumShouldMatch("70%");
		SearchResponse searchResponse = client().prepareSearch()
				.setQuery(multiMatchQuery).get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("1"));

		multiMatchQuery.minimumShouldMatch("30%");
		searchResponse = client().prepareSearch().setQuery(multiMatchQuery)
				.get();
		assertHitCount(searchResponse, 2l);
		assertFirstHit(searchResponse, hasId("1"));
		assertSecondHit(searchResponse, hasId("2"));

		multiMatchQuery.useDisMax(false);
		multiMatchQuery.minimumShouldMatch("70%");
		searchResponse = client().prepareSearch().setQuery(multiMatchQuery)
				.get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("1"));

		multiMatchQuery.minimumShouldMatch("30%");
		searchResponse = client().prepareSearch().setQuery(multiMatchQuery)
				.get();
		assertHitCount(searchResponse, 2l);
		assertFirstHit(searchResponse, hasId("1"));
		assertSecondHit(searchResponse, hasId("2"));

		multiMatchQuery = multiMatchQuery("value1 value2 bar", "field1");
		multiMatchQuery.minimumShouldMatch("100%");
		searchResponse = client().prepareSearch().setQuery(multiMatchQuery)
				.get();
		assertHitCount(searchResponse, 0l);

		multiMatchQuery.minimumShouldMatch("70%");
		searchResponse = client().prepareSearch().setQuery(multiMatchQuery)
				.get();
		assertHitCount(searchResponse, 1l);
		assertFirstHit(searchResponse, hasId("1"));
	}

}
