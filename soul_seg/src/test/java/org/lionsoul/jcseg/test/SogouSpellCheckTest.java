package org.lionsoul.jcseg.test;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Version;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.soul.elasticSearch.plugin.SoulSpellChecker;

@Test
public class SogouSpellCheckTest {
	private static Log log = LogFactory.getLog(SogouSpellCheckTest.class);
	private SoulSpellChecker spellChecker = null;
	private String inPath = "/mnt/f/tmp/SogouLabDic.dic";
	String outPath = "/mnt/f/tmp/Sogou.dic";
	private float accuracy = 0.8f;
	@BeforeClass
	public void start() throws IOException {
		Directory directory = null;

		directory = new RAMDirectory(); // use ram directory
		IndexWriterConfig iwConfig = new IndexWriterConfig(
				Version.LUCENE_CURRENT, null);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
		spellChecker = new SoulSpellChecker(directory);
		spellChecker.indexDictionary(
				new PlainTextDictionary(new File(outPath)), iwConfig, false);
		spellChecker.setAccuracy(accuracy);
	}

	@AfterClass
	public void close() {
		try {
			log.info("spellChecker is closed!");
			spellChecker.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// @IgnoreTest
	// @Test
	// public void extractData() {
	// String temp = null;
	// int num = 0;
	// try {
	// File outFile = new File(outPath);
	// if (outFile.exists())
	// outFile.delete();
	// InputStream in = new FileInputStream(inPath);
	// FileWriter fw = new FileWriter(outPath, false);
	// BufferedWriter bw = new BufferedWriter(fw);
	// BufferedReader reader = new BufferedReader(new InputStreamReader(
	// in, "gbk"));
	// TreeMap<Integer, HashSet<String>> map = new TreeMap<Integer,
	// HashSet<String>>();
	// while ((temp = reader.readLine()) != null) {
	// String[] strs = temp.split("	");
	// String str1 = strs[0].trim();
	// int len = str1.length();
	// HashSet<String> set = map.get(len);
	// if (set == null) {
	// set = new HashSet<String>();
	// set.add(str1);
	// } else {
	// if (!set.contains(str1))
	// set.add(str1);
	// else {
	// log.info(str1);
	// }
	// }
	// map.put(len, set);
	// num++;
	// }
	// for (Integer key : map.keySet()) {
	// HashSet<String> set = map.get(key);
	// log.info("len = " + key + ", size = " + set.size());
	// Iterator<String> it = set.iterator();
	// while (it.hasNext()) {
	// bw.write(it.next() + "\n");
	// }
	// }
	// bw.close();
	// fw.close();
	// log.info("Total Word count is " + num);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// @Ignore
	// @Test
	// public void scanDictionary() {
	// String dicPath = outPath;
	// try {
	// Dictionary dict = new PlainTextDictionary(new File(dicPath));
	// BytesRefIterator iter = dict.getWordsIterator();
	// BytesRef currentTerm;
	// while ((currentTerm = iter.next()) != null) {
	// String word = currentTerm.utf8ToString();
	// int len = word.length();
	// if (len > 0) {
	// log.info("word = " + word.length() + "," + word);
	// if (len < 3) {
	// continue; // too short we bail but "too long" is fine...
	// }
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// @Ignore
	@Test
	public void test1() throws IOException {
		log.info("I don't know what you said: ");
	}
	// @Ignore
	@Test
	public void sogouSpellcheck() {

		int number = 10;
		String[] queStrs = {"麻la将", "种植呵大", "关羽字云长", "麻辣ji翅"};
		try {
			for (String str : queStrs) {
				log.info("I don't know what you said: " + str);
				String[] suggestions = spellChecker.suggestSimilar(str, number);
				if (suggestions == null || suggestions.length == 0) {
					log.info("I don't know what you said: " + str);
				} else {
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < suggestions.length; i++) {
						if (i != (suggestions.length - 1))
							builder.append(suggestions[i] + "/");
						else
							builder.append(suggestions[i]);
					}
					log.info("Did you mean: " + builder.toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
