package org.soul.test;

import java.io.*;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Version;
import org.soul.elasticSearch.plugin.SoulSpellCheck;

public class spellCheck {

	private SoulSpellCheck spellChecker = null;

	public spellCheck(String dictionary) {
		Directory directory = null;
		try {
			directory = new RAMDirectory(); // use ram directory
			IndexWriterConfig iwConfig = new IndexWriterConfig(
					Version.LUCENE_CURRENT, null);
			iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
			spellChecker = new SoulSpellCheck(directory);
			spellChecker.indexDictionary(new PlainTextDictionary(new File(
					dictionary)), iwConfig, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public spellCheck(String spellCheckIndexPath, String dicPath) {
		Directory directory;
		try {
			directory = FSDirectory.open(new File(spellCheckIndexPath));

			spellChecker = new SoulSpellCheck(directory);

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
			Dictionary dict = new PlainTextDictionary(new File(dicPath));
			BytesRefIterator iter = dict.getWordsIterator();
			BytesRef currentTerm;
			while ((currentTerm = iter.next()) != null) {
				String word = currentTerm.utf8ToString();
				int len = word.length();
				System.out.println("word = " + word.length() + "," + word);
				if (len < 3) {
					continue; // too short we bail but "too long" is fine...
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

		String spellIndexPath = "/mnt/f/tmp/lucene-5";
		String dictionaryPath = "/mnt/f/tmp/lucene-dict.txt";
		scanDictionary(dictionaryPath);
		// spellCheck checker = new spellCheck(spellIndexPath, idcFilePath);
		spellCheck checker = new spellCheck(dictionaryPath);
		checker.setAccuracy(0.5f);
		int suggestionsNumber = 15;
		String queryString = "麻辣将";
		// try {
		// indexer.createSpellIndex(spellIndexPath, idcFilePath);
		// indexer.createSpellIndex(oriIndexPath, fieldName, spellIndexPath);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		String[] result = checker.search(queryString, suggestionsNumber);
		if (result == null || result.length == 0) {
			System.out.println("I don't know what you said!");
		} else {
			System.out.println("Did you mean: ");
			for (int i = 0; i < result.length; i++) {
				System.out.println(result[i]);
			}
		}
	}
}
