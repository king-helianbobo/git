package org.suggest.elasticsearch.service;

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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.splitword.lionsoul.jcseg.JcSegment;
import org.splitword.soul.analysis.BasicAnalysis;
import org.splitword.soul.utility.JcsegInstance;
import org.elasticsearch.plugin.EsStaticValue;
import org.elasticsearch.plugin.SoulJcsegAnalyzer;
import org.elasticsearch.plugin.SoulPinyinAnalyzer;
public class SoulTitleCache implements java.io.Closeable {
	private static Log log = LogFactory.getLog(SoulTitleCache.class);
	private static final String Word_Field = "wordField";

	private final Object searcherLock = new Object();
	private final Object modifyCurrentIndexLock = new Object();
	private volatile boolean closed = false;

	Directory titleIndex;
	private IndexSearcher searcher;

	public SoulTitleCache(Directory indexDir) throws IOException {
		this.titleIndex = indexDir;
		setTitleIndex(titleIndex);
	}

	public void setTitleIndex(Directory indexDir) throws IOException {
		// this could be the same directory as the current spellIndex
		// modifications to the directory should be synchronized
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			if (!DirectoryReader.indexExists(indexDir)) {
				IndexWriter writer = new IndexWriter(
						indexDir,
						new IndexWriterConfig(EsStaticValue.LuceneVersion, null));
				writer.close();
			}
			swapSearcher(indexDir);
		}
	}

	public void clearIndex() throws IOException {
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			final Directory dir = this.titleIndex;
			final IndexWriter writer = new IndexWriter(dir,
					new IndexWriterConfig(EsStaticValue.LuceneVersion, null)
							.setOpenMode(OpenMode.CREATE));
			writer.close();
			swapSearcher(dir);
		}
	}

	/**
	 * Check whether the word exists in the index.
	 */
	public boolean exist(String word) throws IOException {
		// obtainSearcher calls ensureOpen
		final IndexSearcher indexSearcher = obtainSearcher();
		try {
			return indexSearcher.getIndexReader().docFreq(
					new Term(Word_Field, word)) > 0;
		} finally {
			releaseSearcher(indexSearcher);
		}
	}

	public String[] suggestTitles(String keyword, int titleNum)
			throws IOException {
		List<String> terms = BasicAnalysis.parse(keyword, EsStaticValue.filter);
		BooleanQuery query = new BooleanQuery();
		if (terms.isEmpty())
			return null;
		else {
			float score = 0.0f;
			for (int i = 0; i < terms.size(); i++) {
				String value = terms.get(i);
				Query tq = new TermQuery(new Term(Word_Field, value));
				score += 1.0f;
				tq.setBoost(score);
				log.info("value = " + value + " , " + score);
				query.add(new BooleanClause(tq, BooleanClause.Occur.SHOULD));
			}
		}
		IndexSearcher indexSearcher = this.obtainSearcher();
		TopDocs topDocs = indexSearcher.search(query, titleNum);
		log.info("Hit: " + topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		if (topDocs == null || topDocs.totalHits <= 0)
			return null;
		int number = Math.min(titleNum, topDocs.totalHits);
		String[] list = new String[number * 2];
		for (int i = 0; i < list.length; i += 2) {
			Document targetDoc = indexSearcher.doc(scoreDocs[i].doc);
			float score = scoreDocs[i].score;
			String value = targetDoc.get(Word_Field);
			// StringBuilder builder = new StringBuilder();
			log.info("Score: " + score + " Document content: " + value);
			list[i] = String.valueOf(score);
			list[i + 1] = value;
		}
		return list;
	}
	/**
	 * Indexes the data from the given {@link Dictionary}.
	 */
	public final void fillThisDictionary(Dictionary dictionary,
			boolean fullMerge) throws IOException {
		Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		analyzerMap.put(Word_Field, new SoulPinyinAnalyzer());
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
				new StandardAnalyzer(EsStaticValue.LuceneVersion), analyzerMap);
		IndexWriterConfig iwConfig = new IndexWriterConfig(
				EsStaticValue.LuceneVersion, wrapper);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);

		final Directory dir = this.titleIndex;
		final IndexWriter writer = new IndexWriter(dir, iwConfig);

		log.info("here is executed!");

		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			log.info("here is executed!  1");

			IndexSearcher indexSearcher = obtainSearcher();
			log.info("here is executed!  2");
			final List<TermsEnum> termsEnums = new ArrayList<TermsEnum>();
			final IndexReader reader = searcher.getIndexReader();

			// get this index's reader
			log.info("here is executed   3 !");
			if (reader.maxDoc() > 0) {
				for (final AtomicReaderContext ctx : reader.leaves()) {
					Terms terms = ctx.reader().terms(Word_Field);
					if (terms != null)
						termsEnums.add(terms.iterator(null));
				}
			}
			boolean isEmpty = termsEnums.isEmpty();
			try {
				BytesRefIterator iter = dictionary.getWordsIterator();
				BytesRef currentTerm;
				log.info("here is executed   4!");
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
					Document doc = new Document();
					doc.add(new TextField(Word_Field, word, Field.Store.YES));
					writer.addDocument(doc);
				}
			} finally {
				releaseSearcher(indexSearcher);
			}
			if (fullMerge) {
				writer.forceMerge(1);
			}
			writer.close();
			swapSearcher(dir);
		}
	}

	private IndexSearcher obtainSearcher() {
		synchronized (searcherLock) {
			ensureOpen();
			searcher.getIndexReader().incRef();
			return searcher;
		}
	}

	private void releaseSearcher(final IndexSearcher aSearcher)
			throws IOException {
		// don't check if open - always decRef
		// don't decrement the private searcher - could have been swapped
		aSearcher.getIndexReader().decRef();
	}

	private void ensureOpen() {
		if (closed) {
			throw new AlreadyClosedException("SoulTitleCache has been closed");
		}
	}

	/**
	 * Close the IndexSearcher used by this SpellChecker
	 * 
	 * @throws IOException
	 *             if the close operation causes an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the {@link SoulTitleCache} is already closed
	 */
	@Override
	public void close() throws IOException {
		synchronized (searcherLock) {
			ensureOpen();
			closed = true;
			if (searcher != null) {
				searcher.getIndexReader().close();
			}
			searcher = null;
		}
	}

	private void swapSearcher(final Directory dir) throws IOException {
		final IndexSearcher indexSearcher = createSearcher(dir);
		synchronized (searcherLock) {
			if (closed) {
				indexSearcher.getIndexReader().close();
				throw new AlreadyClosedException("Spellchecker has been closed");
			}
			if (searcher != null) {
				searcher.getIndexReader().close();
			}
			// set the spellindex in the sync block - ensure consistency
			searcher = indexSearcher;
			this.titleIndex = dir;
		}
	}

	/**
	 * Creates a new read-only IndexSearcher
	 * 
	 * @param dir
	 *            the directory used to open the searcher
	 * @return a new read-only IndexSearcher
	 * @throws IOException
	 *             f there is a low-level IO error
	 */
	// for testing purposes
	IndexSearcher createSearcher(final Directory dir) throws IOException {
		return new IndexSearcher(DirectoryReader.open(dir));
	}

	boolean isClosed() {
		return closed;
	}

}
