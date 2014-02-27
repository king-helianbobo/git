package org.soul.elasticsearch.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.HighFrequencyDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.elasticsearch.plugin.*;
import org.splitword.soul.analysis.BasicAnalysis;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SoulTitleCacheTest {

	String[] texts = {
			"厦门大学的大厦是长春人建立的",
			"秋后的蚂蚱长不了",
			"最美丽     乡村  	教师",
			"百家讲坛开播了",
			"曹洪手下的宾客在满宠辖界内多次犯法，被满宠收捕治罪。曹洪为此写信给满宠，满宠不加理会。尚书令荀彧、少府孔融等人都嘱咐满宠说：“只应讯问，不要滥加拷打。”满宠对此毫无回应，仍按照常法拷问。",
			"满宠深受曹操的器重，并因屡建功勋而被赏赐封爵。曾以关内侯的身份，两次任南阳太守，所在又政绩斐然。",
			"关羽以水急攻襄阳，在此危急时刻，满宠认为必须先稳住军心，然后分析了关羽军队的弱点，明确提出了退敌的奇计妙策。",
			"满宠不置产业，家中没有多余的财物，皇帝下诏说：“你在外领兵作战，专一操心公事，有行父、祭遵的风范。”",
			"满宠字伯宁,山阳昌邑人也,魏国名将，最初在曹操手下任许县县令。", "掌管司法，以执法严格著称.",
			"转任汝南太守，开始参与军事，曾参与赤壁之战。", "后关羽围攻樊城，满宠协助曹仁守城，劝阻了弃城而逃的计划，成功坚持到援军到来。",
			"曹丕在位期间，满宠驻扎在新野，负责荆州侧的对吴作战。", "曹睿在位期间，满宠转任到扬州，接替曹休负责东线对吴作战，屡有功劳",
			"后因年迈调回中央担任太尉，数年后病逝。"};
	private static Log log = LogFactory.getLog(SoulTitleCacheTest.class);
	private static final String pinyinField = "pinyinField";
	private static final String wordField = "wordField";
	private static final String idField = "ID";
	private static final String indexPath = "/mnt/f/tmp/Lucene-2";
	Directory ramDirectory = null;
	IndexReader indexReader = null;
	IndexSearcher indexSearcher = null;

	public void createIndex(Directory directory) {
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		analyzerMap.put(pinyinField, new SoulPinyinAnalyzer());
		analyzerMap.put(wordField, new SoulJcsegAnalyzer());
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
				new StandardAnalyzer(EsStaticValue.LuceneVersion), analyzerMap);
		IndexWriter indexWriter = null;
		IndexWriterConfig iwConfig = new IndexWriterConfig(
				EsStaticValue.LuceneVersion, wrapper);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try {
			indexWriter = new IndexWriter(directory, iwConfig);
			for (int i = 0; i < texts.length; i++) {
				Document doc = new Document();
				doc.add(new StringField(idField, String.valueOf(i + 1),
						Field.Store.YES));
				doc.add(new TextField(pinyinField, texts[i], Field.Store.YES));
				// doc.add(new TextField(wordField, texts[i], Field.Store.YES));
				indexWriter.addDocument(doc);
			}
			indexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testMethod3() throws IOException {
		Directory directory = FSDirectory.open(new File(indexPath));
		createIndex(directory);
	}

	@BeforeClass
	public void beforeClass() {
		// ramDirectory = new RAMDirectory();
		// createIndex(ramDirectory);
		// try {
		// indexReader = DirectoryReader.open(ramDirectory);
		// indexSearcher = new IndexSearcher(indexReader);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}
	@Test
	public void testMethod2() {
		try {
			Directory directory = FSDirectory.open(new File(indexPath));
			IndexReader reader = DirectoryReader.open(directory);
			HighFrequencyDictionary dict = new HighFrequencyDictionary(reader,
					idField, 0);
			IndexSearcher searcher = new IndexSearcher(
					DirectoryReader.open(directory));
			fillThisDictionary(dict, searcher);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// @Test
	public void testMethod4() {
		try {
			Directory directory = FSDirectory.open(new File(indexPath));
			IndexReader reader = DirectoryReader.open(directory);
			Bits liveDocs = MultiFields.getLiveDocs(reader);
			for (int i = 0; i < reader.maxDoc(); i++) {
				if (liveDocs != null && !liveDocs.get(i))
					continue;
				Document doc = reader.document(i);
				log.info(doc.get(idField) + " Document content: "
						+ doc.get(pinyinField));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void fillThisDictionary(Dictionary dictionary,
			IndexSearcher searcher) {
		try {
			final Directory dir = new RAMDirectory();
			Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
			analyzerMap.put(pinyinField, new SoulPinyinAnalyzer());
			PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
					new StandardAnalyzer(EsStaticValue.LuceneVersion),
					analyzerMap);
			IndexWriterConfig iwConfig = new IndexWriterConfig(
					EsStaticValue.LuceneVersion, wrapper);
			iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			final IndexWriter writer = new IndexWriter(dir, iwConfig);
			// IndexReader reader = DirectoryReader.open(dir);
			final List<TermsEnum> termsEnums = new ArrayList<TermsEnum>();
			// if (reader.maxDoc() > 0) {
			// for (final AtomicReaderContext ctx : reader.leaves()) {
			// Terms terms = ctx.reader().terms(pinyinField);
			// if (terms != null)
			// termsEnums.add(terms.iterator(null));
			// }
			// }
			boolean isEmpty = termsEnums.isEmpty();
			BytesRefIterator iter = dictionary.getWordsIterator();
			BytesRef currentTerm;
			terms : while ((currentTerm = iter.next()) != null) {
				String word = currentTerm.utf8ToString();
				log.info("This word is : " + word);
				if ((word == null) || (word.equals("null"))
						|| (word.equals("")))
					continue;
				if (!isEmpty) {
					for (TermsEnum te : termsEnums) {
						if (te.seekExact(currentTerm)) {
							continue terms;
						}
					}
				}
				Term term = new Term(idField, word);
				Query termQuery = new TermQuery(term);
				TopDocs topDocs = searcher.search(termQuery, 100);
				log.info("Hit: " + topDocs.totalHits);
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				if (topDocs == null || topDocs.totalHits <= 0)
					continue;
				for (int i = 0; i < topDocs.totalHits; i++) {
					Document targetDoc = searcher.doc(scoreDocs[i].doc);
					StringBuilder builder = new StringBuilder();
					log.info("Score: " + scoreDocs[i].score
							+ " Document content: "
							+ targetDoc.get(pinyinField) + " ID: "
							+ targetDoc.get(idField));
				}
				// Document doc = new Document();
				// doc.add(new TextField(pinyinField, word, Field.Store.YES));
				// writer.addDocument(doc);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// @Test
	public void testMethod1() {
		try {
			String[] keywords = {"厦门大学", "满宠南阳下任", "guany"};
			for (String keyword : keywords) {
				List<String> terms = BasicAnalysis.parse(keyword,
						EsStaticValue.filter);
				BooleanQuery query = new BooleanQuery();
				if (terms.isEmpty())
					continue;
				else {
					float score = 0.0f;
					for (int i = 0; i < terms.size(); i++) {
						String value = terms.get(i);
						Query tq = new TermQuery(new Term(pinyinField, value));
						score += 1.0f;
						tq.setBoost(score);
						log.info("value = " + value + " , " + score);
						query.add(new BooleanClause(tq,
								BooleanClause.Occur.SHOULD));
					}
				}
				TopDocs topDocs = indexSearcher.search(query, 100);
				log.info("Hit: " + topDocs.totalHits);
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				if (topDocs == null || topDocs.totalHits <= 0)
					continue;
				for (int i = 0; i < topDocs.totalHits; i++) {
					Document targetDoc = indexSearcher.doc(scoreDocs[i].doc);
					StringBuilder builder = new StringBuilder();
					log.info("Score: " + scoreDocs[i].score
							+ " Document content: "
							+ targetDoc.get(pinyinField));
				}
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public void afterClass() {
		if (indexReader != null) {
			try {
				indexReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (ramDirectory != null) {
			try {
				ramDirectory.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
