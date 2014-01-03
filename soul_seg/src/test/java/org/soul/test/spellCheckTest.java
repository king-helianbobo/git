package org.soul.test;

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Version;
import org.soul.elasticSearch.plugin.SoulSpellChecker;

public class spellCheckTest {
	private static Log log = LogFactory.getLog(spellCheckTest.class);
	private SoulSpellChecker spellChecker = null;

	public spellCheckTest(String dictionary) {
		Directory directory = null;
		try {
			directory = new RAMDirectory(); // use ram directory
			IndexWriterConfig iwConfig = new IndexWriterConfig(
					Version.LUCENE_CURRENT, null);
			iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
			spellChecker = new SoulSpellChecker(directory);
			spellChecker.indexDictionary(new PlainTextDictionary(new File(
					dictionary)), iwConfig, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public spellCheckTest(String spellCheckIndexPath, String dicPath) {
		Directory directory;
		try {
			directory = FSDirectory.open(new File(spellCheckIndexPath));
			spellChecker = new SoulSpellChecker(directory);
			IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_CURRENT, null);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
			spellChecker.indexDictionary(new PlainTextDictionary(new File(
					dicPath)), config, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void scanDictionary(String dicPath) {
		try {
			// Dictionary dict1 = new LuceneDictionary(null, dicPath);
			Dictionary dict = new PlainTextDictionary(new File(dicPath));
			BytesRefIterator iter = dict.getWordsIterator();
			BytesRef currentTerm;
			while ((currentTerm = iter.next()) != null) {
				String word = currentTerm.utf8ToString();
				int len = word.length();
				if (!word.equals("")) {
					log.info("word = " + word.length() + "," + word);
					if (len < 3) {
						continue; // too short we bail but "too long" is fine...
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setAccuracy(float v) {
		spellChecker.setAccuracy(v);
	}

	public String[] search(String queryString, int suggestionsNumber) {
		String[] suggestions = null;
		try {
			// if (exist(queryString))
			// return null;
			suggestions = spellChecker.suggestSimilar(queryString,
					suggestionsNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return suggestions;
	}

	public static void main(String[] args) {

		// String spellIndexPath = "/mnt/f/tmp/lucene-5";
		String dictionaryPath = "/mnt/f/tmp/lucene-dict.txt";
		scanDictionary(dictionaryPath);
		// spellCheck checker = new spellCheck(spellIndexPath, dictionaryPath);
		spellCheckTest checker = new spellCheckTest(dictionaryPath);
		checker.setAccuracy(0.9f);
		int suggestionsNumber = 15;
		// String queryString = "麻辣将";
		// String[] queStrs = {"麻la将", "种植呵大", "关羽字云长", "麻辣ji翅"};
		String[] queStrs = {"中文测试"};
		for (String str : queStrs) {
			String[] result = checker.search(str, suggestionsNumber);
			if (result == null || result.length == 0) {
				log.info("I don't know what you said: " + str);
			} else {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < result.length; i++) {
					if (i != (result.length - 1))
						builder.append(result[i] + "/");
					else
						builder.append(result[i]);
				}
				log.info("Did you mean: " + builder.toString());
			}
		}
	}
}
