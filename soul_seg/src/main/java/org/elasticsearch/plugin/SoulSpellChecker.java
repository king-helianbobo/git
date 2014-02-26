package org.elasticsearch.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.document.StringField;
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
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.splitword.lionsoul.jcseg.JcSegment;
import org.splitword.soul.utility.JcsegInstance;

public class SoulSpellChecker implements java.io.Closeable {
	private static Log log = LogFactory.getLog(SoulSpellChecker.class);
	private static JcSegment jcSeg = JcsegInstance.instance();
	private float accuracy = -0.5f;
	private static final String F_WORD = "word";
	private static final String F_PINYIN = "soulPinyin";
	private static final String pinyinGram = "pinyinGram";
	private static final String pinyinStart = "pinyinStart";
	private static final String pinyinEnd = "pinyinEnd";
	private static final String chineseGram = "chineseGram";
	private static final String chineseStart = "chineseStart";
	private static final String chineseEnd = "chineseEnd";

	Directory spellIndex;
	private float bStart = 2.0f; // boost value for start
	private float bEnd = 1.0f; // boost value for end
	private IndexSearcher searcher;
	private final Object searcherLock = new Object();
	private final Object modifyCurrentIndexLock = new Object();
	private volatile boolean closed = false;
	// minimum score for hits generated by the spell checker query
	private StringDistance sd1 = new NGramDistance();
	private StringDistance sd;
	private Comparator<SuggestWord> comparator;

	public SoulSpellChecker(Directory spellIndex, StringDistance sd)
			throws IOException {
		this(spellIndex, sd, SuggestWordQueue.DEFAULT_COMPARATOR);
	}

	public SoulSpellChecker(Directory spellIndex) throws IOException {
		this(spellIndex, new LevensteinDistance());
	}

	public SoulSpellChecker(Directory spellIndex, StringDistance sd,
			Comparator<SuggestWord> comparator) throws IOException {
		setSpellIndex(spellIndex);
		setStringDistance(sd);
		this.comparator = comparator;
	}

	/**
	 * Use a different index as the spell checker index or re-open the existing
	 * index if <code>spellIndex</code> is the same value as given in the
	 * constructor.
	 * 
	 * @param spellIndexDir
	 *            the spell directory to use
	 * @throws AlreadyClosedException
	 *             if the Spellchecker is already closed
	 * @throws IOException
	 *             if spellchecker can not open the directory
	 */
	// TODO: we should make this final as it is called in the constructor
	public void setSpellIndex(Directory spellIndexDir) throws IOException {
		// this could be the same directory as the current spellIndex
		// modifications to the directory should be synchronized
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			if (!DirectoryReader.indexExists(spellIndexDir)) {
				IndexWriter writer = new IndexWriter(
						spellIndexDir,
						new IndexWriterConfig(EsStaticValue.LuceneVersion, null));
				writer.close();
			}
			swapSearcher(spellIndexDir);
		}
	}

	public void setComparator(Comparator<SuggestWord> comparator) {
		this.comparator = comparator;
	}

	public Comparator<SuggestWord> getComparator() {
		return comparator;
	}

	public void setStringDistance(StringDistance sd) {
		this.sd = sd;
	}

	public StringDistance getStringDistance() {
		return sd;
	}

	public void setAccuracy(float acc) {
		this.accuracy = acc;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public String[] suggestSimilar(String word, int numSug) throws IOException {
		if (this.accuracy <= 0.0f)
			return this.suggestWithFixedNumber(word, numSug, null, null,
					SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
		else
			return this.suggestSimilar(word, numSug, null, null,
					SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
	}

	public String[] suggestSimilar(String word, int numSug, float accuracy)
			throws IOException {
		this.setAccuracy(accuracy);
		if (this.accuracy <= 0.0f)
			return this.suggestWithFixedNumber(word, numSug, null, null,
					SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
		else
			return this.suggestSimilar(word, numSug, null, null,
					SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX, accuracy);
	}

	private String[] suggestSimilar(String word, int numSug, IndexReader ir,
			String field, SuggestMode suggestMode) throws IOException {
		return suggestSimilar(word, numSug, ir, field, suggestMode,
				this.accuracy);
	}

	/**
	 * Suggest similar words，前一部分为推荐单词，后半部分为每个单词的分数
	 */
	private String[] suggestSimilar(String word, int numSuggest,
			IndexReader ir, String field, SuggestMode suggestMode, float accur)
			throws IOException {
		final IndexSearcher indexSearcher = obtainSearcher();
		try {
			if (ir == null || field == null) {
				suggestMode = SuggestMode.SUGGEST_ALWAYS;
			}
			if (suggestMode == SuggestMode.SUGGEST_ALWAYS) {
				ir = null;
				field = null;
			}
			final int freq = (ir != null && field != null) ? ir
					.docFreq(new Term(field, word)) : 0;
			final int goalFreq = suggestMode == SuggestMode.SUGGEST_MORE_POPULAR
					? freq
					: 0;

			if (suggestMode == SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX
					&& freq > 0) {
				return new String[]{word};
			}
			String pinyin = jcSeg.convertToPinyin(word);
			// 获得当前Term的拼音
			BooleanQuery query = createBooleanQuery(word, pinyin);
			int maxHits = 5 * numSuggest;
			ScoreDoc[] hits = indexSearcher.search(query, null, maxHits).scoreDocs;
			SuggestWordQueue sugQueue = new SuggestWordQueue(numSuggest,
					comparator);
			int finalLen = Math.min(hits.length, maxHits);
			for (int i = 0; i < finalLen; i++) {
				SuggestWord sugWord = new SuggestWord();
				sugWord.string = indexSearcher.doc(hits[i].doc).get(F_WORD);
				String sugWordPinyin = indexSearcher.doc(hits[i].doc).get(
						F_PINYIN);
				// don't suggest a word for itself, that would be silly
				if (sugWord.string.equals(word)) {
					continue;
				}
				// 获得编辑距离，当前编辑距离只考虑到拼音，还没有对汉字字符串的考虑
				sugWord.score = sd.getDistance(word, sugWord.string);
				float score = sd.getDistance(pinyin, sugWordPinyin);
				float score1 = sd1.getDistance(word, sugWord.string);
				sugWord.score = Math.max(sugWord.score, score);
				sugWord.score = Math.max(sugWord.score, score1);
				if (sugWord.score < accur) {
					continue;
				}
				if (ir != null && field != null) { // use the user index
					sugWord.freq = ir.docFreq(new Term(field, sugWord.string));
					// don't suggest a word that is not present in the field
					if ((suggestMode == SuggestMode.SUGGEST_MORE_POPULAR && goalFreq > sugWord.freq)
							|| sugWord.freq < 1) {
						continue;
					}
				}
				sugQueue.insertWithOverflow(sugWord);
				if ((sugQueue.size() == numSuggest) && (accur > 0.0f)) {
					// if queue full, maintain the minScore score
					accur = sugQueue.top().score;
				}
			}
			String[] list = new String[sugQueue.size() * 2];
			for (int i = list.length - 1; i >= 0; i -= 2) {
				float suggestScore = sugQueue.top().score;
				String suggestWord = sugQueue.top().string;
				list[i] = String.valueOf(suggestScore);
				list[i - 1] = suggestWord;
				sugQueue.pop();
			}
			return list;
		} finally {
			releaseSearcher(indexSearcher);
		}
	}

	/**
	 * 返回与word相似度最高的前numSuggest个word，当accuracy为负数时，调用此函数
	 * 
	 * @author LiuBo
	 * @since 2014年1月26日
	 * @param word
	 * @param numSuggest
	 * @param ir
	 * @param field
	 * @param suggestMode
	 * @param accuracy
	 * @return
	 * @throws IOException
	 *             String[]
	 */
	private String[] suggestWithFixedNumber(String word, int numSuggest,
			IndexReader ir, String field, SuggestMode suggestMode)
			throws IOException {
		return this.suggestSimilar(word, numSuggest, null, null,
				SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX, this.accuracy);
	}

	private BooleanQuery createBooleanQuery(String word, String pinyin) {
		BooleanQuery query = new BooleanQuery();
		String[] grams;
		String key;
		int len = pinyin.length();
		for (int ng = getPinyinMin(len); ng <= getPinyinMax(len); ng++) {
			grams = formGrams(pinyin, ng);
			if (grams.length == 0) {
				continue;
			}
			if (bStart > 0) { // should we boost prefixes?
				add(query, pinyinStart + ng, grams[0], bStart);
			}
			if (bEnd > 0) { // should we boost suffixes?
				add(query, pinyinEnd + ng, grams[grams.length - 1], bEnd);
			}
			key = pinyinGram + ng;
			for (int i = 0; i < grams.length; i++) {
				add(query, key, grams[i]);
			}
		}
		for (int ng = 2; ng <= 3; ng++) {
			grams = formGrams(word, ng);
			if (grams.length == 0) {
				continue;
			}
			if (bStart > 0) { // should we boost prefixes?
				add(query, chineseStart + ng, grams[0], bStart);
			}
			if (bEnd > 0) { // should we boost suffixes?
				add(query, chineseEnd + ng, grams[grams.length - 1], bEnd);
			}
			key = chineseGram + ng; // form key
			for (int i = 0; i < grams.length; i++) {
				add(query, key, grams[i]);
			}
		}
		return query;
	}

	/**
	 * Add a clause to a boolean query.
	 */
	private static void add(BooleanQuery q, String name, String value,
			float boost) {
		Query tq = new TermQuery(new Term(name, value));
		tq.setBoost(boost);
		q.add(new BooleanClause(tq, BooleanClause.Occur.SHOULD));
	}

	private static void add(BooleanQuery q, String name, String value) {
		q.add(new BooleanClause(new TermQuery(new Term(name, value)),
				BooleanClause.Occur.SHOULD));
	}

	private static String[] formGrams(String text, int ng) {
		int len = text.length();
		String[] res = new String[len - ng + 1];
		for (int i = 0; i < len - ng + 1; i++) {
			res[i] = text.substring(i, i + ng);
		}
		return res;
	}

	/**
	 * Removes all terms from the spell check index.
	 * 
	 * @throws IOException
	 *             If there is a low-level I/O error.
	 * @throws AlreadyClosedException
	 *             if the SpellChecker is already closed
	 */
	public void clearIndex() throws IOException {
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			final Directory dir = this.spellIndex;
			final IndexWriter writer = new IndexWriter(dir,
					new IndexWriterConfig(EsStaticValue.LuceneVersion, null)
							.setOpenMode(OpenMode.CREATE));
			writer.close();
			swapSearcher(dir);
		}
	}

	/**
	 * Check whether the word exists in the index.
	 * 
	 * @param word
	 *            word to check
	 * @throws IOException
	 *             If there is a low-level I/O error.
	 * @throws AlreadyClosedException
	 *             if the SpellChecker is already closed
	 * @return true if the word exists in the index
	 */
	public boolean exist(String word) throws IOException {
		// obtainSearcher calls ensureOpen
		final IndexSearcher indexSearcher = obtainSearcher();
		try {
			return indexSearcher.getIndexReader().docFreq(
					new Term(F_WORD, word)) > 0;
		} finally {
			releaseSearcher(indexSearcher);
		}
	}

	/**
	 * Indexes the data from the given {@link Dictionary}.
	 */
	public final void indexDictionary(Dictionary dict,
			IndexWriterConfig config, boolean fullMerge) throws IOException {
		synchronized (modifyCurrentIndexLock) {
			ensureOpen();
			final Directory dir = this.spellIndex;
			final IndexWriter writer = new IndexWriter(dir, config);
			IndexSearcher indexSearcher = obtainSearcher();
			final List<TermsEnum> termsEnums = new ArrayList<TermsEnum>();
			final IndexReader reader = searcher.getIndexReader();
			// get this index's reader
			if (reader.maxDoc() > 0) {
				for (final AtomicReaderContext ctx : reader.leaves()) {
					Terms terms = ctx.reader().terms(F_WORD);
					if (terms != null)
						termsEnums.add(terms.iterator(null));
				}
			}
			boolean isEmpty = termsEnums.isEmpty();
			try {
				BytesRefIterator iter = dict.getWordsIterator();
				BytesRef currentTerm;
				terms : while ((currentTerm = iter.next()) != null) {
					String word = currentTerm.utf8ToString();
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
					String pinyin = jcSeg.convertToPinyin(word);
					int len = pinyin.length();
					if (len < 2)
						continue; // ignore too short pinyin but "long" is fine
					// now index word
					Document doc = createDocument(word, 2, 3, pinyin,
							getPinyinMin(len), getPinyinMax(len));
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

	private static int getPinyinMin(int length) {
		if (length > 5) {
			return 4;
		}
		if (length == 5) {
			return 3;
		}
		return 2;
	}

	private static int getPinyinMax(int length) {
		if (length > 5) {
			return 5;
		}
		if (length == 5) {
			return 4;
		}
		return 3;
	}

	private static Document createDocument(String text, int ng1, int ng2,
			String pinyin, int pinyin_ng1, int pinyin_ng2) {
		Document doc = new Document();
		// word field is never queried on... its indexed so it can be
		// quickly checked for rebuild (and stored for retrieval). Doesn't need
		// norms or TF/position
		Field f = new StringField(F_WORD, text, Field.Store.YES);
		doc.add(f); // original term
		f = new StringField(F_PINYIN, pinyin, Field.Store.YES);
		doc.add(f); // add pinyin form
		addChineseGram(text, doc, ng1, ng2);
		addPinyinGram(pinyin, doc, pinyin_ng1, pinyin_ng2);
		return doc;
	}

	private static void addChineseGram(String text, Document doc, int ng1,
			int ng2) {
		int len = text.length();
		for (int ng = ng1; ng <= ng2; ng++) {
			String key = chineseGram + ng;
			String end = null;
			for (int i = 0; i < len - ng + 1; i++) {
				String gram = text.substring(i, i + ng);
				FieldType ft = new FieldType(StringField.TYPE_NOT_STORED);
				ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
				Field ngramField = new Field(key, gram, ft);
				doc.add(ngramField);
				if (i == 0) {
					Field startField = new StringField(chineseStart + ng, gram,
							Field.Store.NO);
					doc.add(startField);
				}
				end = gram;
			}
			if (end != null) {
				Field endField = new StringField(chineseEnd + ng, end,
						Field.Store.NO);
				doc.add(endField);
			}
		}
	}
	private static void addPinyinGram(String text, Document doc, int ng1,
			int ng2) {
		int len = text.length();
		for (int ng = ng1; ng <= ng2; ng++) {
			String key = pinyinGram + ng;
			String end = null;
			for (int i = 0; i < len - ng + 1; i++) {
				String gram = text.substring(i, i + ng);
				FieldType ft = new FieldType(StringField.TYPE_NOT_STORED);
				ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
				Field ngramField = new Field(key, gram, ft);
				doc.add(ngramField);
				if (i == 0) {
					Field startField = new StringField(pinyinStart + ng, gram,
							Field.Store.NO);
					doc.add(startField);
				}
				end = gram;
			}
			if (end != null) {
				Field endField = new StringField(pinyinEnd + ng, end,
						Field.Store.NO);
				doc.add(endField);
			}
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
			throw new AlreadyClosedException("Spellchecker has been closed");
		}
	}

	/**
	 * Close the IndexSearcher used by this SpellChecker
	 * 
	 * @throws IOException
	 *             if the close operation causes an {@link IOException}
	 * @throws AlreadyClosedException
	 *             if the {@link SoulSpellChecker} is already closed
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
		/*
		 * opening a searcher is possibly very expensive. We rather close it
		 * again if the Spellchecker was closed during this operation than block
		 * access to the current searcher while opening.
		 */
		final IndexSearcher indexSearcher = createSearcher(dir);
		synchronized (searcherLock) {
			if (closed) {
				indexSearcher.getIndexReader().close();
				throw new AlreadyClosedException("Spellchecker has been closed");
			}
			if (searcher != null) {
				searcher.getIndexReader().close();
			}
			// set the spellindex in the sync block - ensure consistency.
			searcher = indexSearcher;
			this.spellIndex = dir;
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

	/**
	 * Returns <code>true</code> if and only if the {@link SoulSpellChecker} is
	 * closed, otherwise <code>false</code>.
	 * 
	 * @return <code>true</code> if and only if the {@link SoulSpellChecker} is
	 *         closed, otherwise <code>false</code>.
	 */
	boolean isClosed() {
		return closed;
	}

}
