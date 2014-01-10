package org.soul.elasticsearch.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//import opensource.analyzer.lucene.IKAnalyzer4PinYin;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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
import org.soul.elasticSearch.pinyin.PinyinAnalyzer;
import org.soul.elasticSearch.plugin.SoulIndexAnalyzer;

public class IndexAndSearchDemo {

	public static void main(String[] args) {
		// Lucene Document的域名
		String fieldName = "hanzi";
		String quanpin = "pinyin";
		// String shouzimu = "shouzimu";
		// 检索内容
		// String text =
		// "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
		String text = "厦门大学的大厦是长春人建立的，秋后的蚂蚱长不了。";
		// 实例化IKAnalyzer分词器
		// 使用PerFieldAnalyzerWrapper可以对不同的field使用不同的分词器
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		analyzerMap.put(quanpin, new PinyinAnalyzer());
		// analyzerMap.put(shouzimu, new IKAnalyzer4PinYin(false,
		// IKAnalyzer4PinYin.PINYIN_SHOUZIMU));
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
				new SoulIndexAnalyzer(), analyzerMap);

		Directory directory = null;
		IndexWriter iwriter = null;
		IndexReader ireader = null;
		IndexSearcher isearcher = null;
		try {
			// 建立内存索引对象
			directory = new RAMDirectory();

			// 配置IndexWriterConfig
			IndexWriterConfig iwConfig = new IndexWriterConfig(
					Version.LUCENE_40, wrapper);
			iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
			iwriter = new IndexWriter(directory, iwConfig);
			// 写入索引
			Document doc = new Document();
			doc.add(new StringField("ID", "10000", Field.Store.YES));
			doc.add(new TextField(fieldName, text, Field.Store.YES));
			doc.add(new TextField(quanpin, text, Field.Store.YES));
			// doc.add(new TextField(shouzimu, text, Field.Store.YES));

			iwriter.addDocument(doc);
			iwriter.close();

			// 搜索过程**********************************
			// 实例化搜索器
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);

			String keyword = "南京大学";
			// 使用QueryParser查询分析器构造Query对象

			Analyzer analyzer = new SoulIndexAnalyzer();
			// Analyzer analyzer = new IKAnalyzer4PinYin(true);
			QueryParser qp = new QueryParser(Version.LUCENE_40, fieldName,
					analyzer);
			QueryParser qpQuanpin = new QueryParser(Version.LUCENE_40, quanpin,
					analyzer);
			// QueryParser qpShouzimu = new QueryParser(Version.LUCENE_40,
			// shouzimu, analyzer);

			Query query = qp.parse(keyword);
			Query queryQuanpin = qpQuanpin.parse(keyword);
			// Query queryShouzimu = qpShouzimu.parse(keyword);
			//
			BooleanQuery bq = new BooleanQuery();
			BooleanQuery innerbq = new BooleanQuery();
			//
			bq.add(query, BooleanClause.Occur.SHOULD);
			bq.add(queryQuanpin, BooleanClause.Occur.SHOULD);
			// bq.add(queryShouzimu, BooleanClause.Occur.SHOULD);
			innerbq.add(bq, BooleanClause.Occur.MUST);

			// System.out.println("innerbq = " + innerbq);

			// 搜索相似度最高的5条记录
			TopDocs topDocs = isearcher.search(innerbq, 5);
			System.out.println("命中：" + topDocs.totalHits);
			// 输出结果
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (int i = 0; i < topDocs.totalHits; i++) {
				Document targetDoc = isearcher.doc(scoreDocs[i].doc);
				System.out.println("内容：" + targetDoc.toString());
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
			if (ireader != null) {
				try {
					ireader.close();
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
