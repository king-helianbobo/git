package org.soul.elasticsearch.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
//import static org.hamcrest.CoreMatchers.containsString;

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
}
