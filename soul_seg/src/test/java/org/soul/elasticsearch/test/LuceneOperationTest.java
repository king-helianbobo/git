package org.soul.elasticsearch.test;

import java.io.IOException;
import java.util.HashMap;
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
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.soul.elasticSearch.plugin.*;
import org.testng.annotations.Test;

public class LuceneOperationTest {
	private static Log log = LogFactory.getLog(LuceneOperationTest.class);
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
	@Test
	public void testMethod1() {
		String hanzi = "hanzi";
		String jcsegHanzi = "jcHanzi";
		// String shouzimu = "shouzimu";
		// 使用PerFieldAnalyzerWrapper可以对不同的field使用不同的分词器
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		analyzerMap.put(hanzi, new SoulPinyinAnalyzer());
		analyzerMap.put(jcsegHanzi, new SoulJcsegAnalyzer());
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
				new StandardAnalyzer(Version.LUCENE_CURRENT), analyzerMap);
		Directory directory = null;
		IndexWriter indexWriter = null;
		IndexReader indexReader = null;
		IndexSearcher indexSearcher = null;
		try {
			// 建立内存索引对象
			directory = new RAMDirectory();
			// 配置IndexWriterConfig
			IndexWriterConfig iwConfig = new IndexWriterConfig(
					Version.LUCENE_CURRENT, wrapper);
			iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			indexWriter = new IndexWriter(directory, iwConfig);
			for (int i = 0; i < texts.length; i++) {
				Document doc = new Document();
				doc.add(new StringField("ID", String.valueOf(i + 1),
						Field.Store.YES));
				doc.add(new TextField(hanzi, texts[i], Field.Store.YES));
				doc.add(new TextField(jcsegHanzi, texts[i], Field.Store.YES));
				// doc.add(new TextField(shouzimu, text, Field.Store.YES));
				indexWriter.addDocument(doc);
			}
			indexWriter.close();
			// **********************************
			indexReader = DirectoryReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);

			String[] keywords = {"厦门大学", "满宠", "guany"};
			for (String keyword : keywords) {
				// 使用QueryParser查询分析器构造Query对象
				Analyzer analyzer = new SoulIndexAnalyzer();
				// Analyzer analyzer = new IKAnalyzer4PinYin(true);
				QueryParser qpHanzi = new QueryParser(Version.LUCENE_CURRENT,
						hanzi, analyzer);
				QueryParser qpJcseg = new QueryParser(Version.LUCENE_CURRENT,
						jcsegHanzi, analyzer);
				Query queryHanzi = qpHanzi.parse(keyword);
				Query queryJcseg = qpJcseg.parse(keyword);
				// Query queryShouzimu = qpShouzimu.parse(keyword);
				//
				BooleanQuery bq = new BooleanQuery();
				BooleanQuery innerbq = new BooleanQuery();
				//
				bq.add(queryHanzi, BooleanClause.Occur.SHOULD);
				bq.add(queryJcseg, BooleanClause.Occur.SHOULD);
				// bq.add(queryShouzimu, BooleanClause.Occur.SHOULD);
				innerbq.add(bq, BooleanClause.Occur.MUST);
				int numDoc = 5;
				// search the top 5 most relevant records
				TopDocs topDocs = indexSearcher.search(innerbq, numDoc);
				log.info("Hit: " + topDocs.totalHits);

				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				if (topDocs == null || topDocs.totalHits <= 0)
					continue;
				for (int i = 0; i < Math.min(topDocs.totalHits, numDoc); i++) {
					Document targetDoc = indexSearcher.doc(scoreDocs[i].doc);
					log.info("Document content: " + targetDoc.toString());
				}
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (indexReader != null) {
				try {
					indexReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
