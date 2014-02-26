package org.lionsoul.elasticsearch.test;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Version;
import org.elasticsearch.plugin.SoulSpellChecker;
import org.splitword.lionsoul.jcseg.JcSegment;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class SogouSpellCheckTest {
	private static Log log = LogFactory.getLog(SogouSpellCheckTest.class);
	private SoulSpellChecker spellChecker = null;
	private String inPath = "/mnt/f/tmp/SogouLabDic.dic";
	private String outPath = "/mnt/f/tmp/Sogou.dic";
	private String indexPath = "/mnt/f/tmp/Lucene-1";
	private float accuracy = -0.8f;

	@BeforeClass
	public void start() throws IOException {
		Directory directory = FSDirectory.open(new File(indexPath));
		spellChecker = new SoulSpellChecker(directory);
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

	@Test(enabled = false)
	public void fillRamDirectoryTest() throws IOException {
		Directory directory = new RAMDirectory(); // use ram directory
		IndexWriterConfig iwConfig = new IndexWriterConfig(
				Version.LUCENE_CURRENT, null);
		iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
		spellChecker = new SoulSpellChecker(directory);
		spellChecker.setAccuracy(accuracy);
		spellChecker.indexDictionary(
				new PlainTextDictionary(new File(outPath)), iwConfig, false);
		// String[] strs = { "拳皇oua", "c超", "山陬海噬", "漂亮mm", "绿色和平", "五零四散",
		// "绿脓杆菌" };
		// String[] strs = { "一二三四五个", "五零四散" };
		// for (String str : strs)
		// log.info(jcSeg.convertToPinyin(str));

	}
	@Test(enabled = false)
	public void fillFSDirectoryTest() {
		Directory directory;
		try {
			directory = FSDirectory.open(new File(indexPath));
			spellChecker = new SoulSpellChecker(directory);
			IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_CURRENT, null);
			config.setOpenMode(OpenMode.CREATE_OR_APPEND); // set open mode
			spellChecker.indexDictionary(new PlainTextDictionary(new File(
					outPath)), config, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Test(enabled = false)
	public void extractData() {
		String temp = null;
		int num = 0;
		try {
			File outFile = new File(outPath);
			if (outFile.exists())
				outFile.delete();
			InputStream in = new FileInputStream(inPath);
			FileWriter fw = new FileWriter(outPath, false);
			BufferedWriter bw = new BufferedWriter(fw);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "gbk"));
			TreeMap<Integer, HashSet<String>> map = new TreeMap<Integer, HashSet<String>>();
			while ((temp = reader.readLine()) != null) {
				String[] strs = temp.split("	");
				String str1 = strs[0].trim();
				int len = str1.length();
				HashSet<String> set = map.get(len);
				if (set == null) {
					set = new HashSet<String>();
					set.add(str1);
				} else {
					if (!set.contains(str1))
						set.add(str1);
					else {
						log.info(str1);
					}
				}
				map.put(len, set);
				num++;
			}
			for (Integer key : map.keySet()) {
				HashSet<String> set = map.get(key);
				log.info("len = " + key + ", size = " + set.size());
				Iterator<String> it = set.iterator();
				while (it.hasNext()) {
					bw.write(it.next() + "\n");
				}
			}
			bw.close();
			fw.close();
			log.info("Total Word count is " + num);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test(enabled = true)
	public void sogouSpellcheckTest() {

		int number = 10;
		// String[] queStrs = {"麻la将", "种植呵大", "关羽字云长", "麻辣ji翅"};
		String[] queStrs = {"中央美术","中央书记"};
		try {
			for (String str : queStrs) {
				String[] suggestions = spellChecker.suggestSimilar(str, number);
				if (suggestions == null || suggestions.length == 0) {
					log.info("I don't know what you said: " + str);
				} else {
					StringBuilder builder = new StringBuilder();
					for (int i = 0; i < suggestions.length; i++) {
						if (i % 2 != 0) {
							builder.append("," + suggestions[i] + "]");
						} else {
							if (i != 0)
								builder.append(" [" + suggestions[i]);
							else
								builder.append("[" + suggestions[i]);
						}
					}
					log.info("Did you mean: " + builder.toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
