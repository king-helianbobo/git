package com.elasticsearch.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.util.Version;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.node.internal.InternalSettingsPreparer;

import com.splitword.soul.utility.MyStaticValue;

public class ClientStaticValue {
	private static ESLogger logger = Loggers.getLogger("soul-analyzer");
	public static final Version LuceneVersion = Version.LUCENE_48;
	public static Set<String> stopWordsSet = null;
	public static Map<String, List<String>> synonymTree = null;
	public static boolean pstemming = true;
	private static boolean loaded = false;
	private static Environment environment;
	private static final Lock LOCK = new ReentrantLock();
	public static String synonymPath = "library/synonym-new.txt";
	public static String wordvectorPath = "library/vectors3.txt";

	public static final String TYPE_WORD = "term";
	public static final String TYPE_HANZI = "hanzi";
	public static final String TYPE_SYNONYM = "synonym";
	public static final String TYPE_PINYIN = "pinyin";
	public static final String TYPE_VECTOR = "vector";
	static {
		Tuple<Settings, Environment> tuple = InternalSettingsPreparer
				.prepareSettings(Builder.EMPTY_SETTINGS, true);
		environment = tuple.v2();
		initConfigPath(tuple.v1());
		try {
			loadStopWords();// 装载停用词词典
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadData() {
		if (loaded)
			return;
		LOCK.lock();
		if (loaded) {
			LOCK.unlock();
			return;
		}
		try {
			loadSynonymTree();// load synonym tree
			loadVectorTree(); // load vector tree
			loaded = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			LOCK.unlock();
		}
	}

	private static void initConfigPath(Settings settings) {
		synonymPath = getPath(settings.get("synonymLibPath", synonymPath));
		wordvectorPath = getPath(settings.get("wordvectorPath", wordvectorPath));
		logger.info(synonymPath);
		logger.info(wordvectorPath);
	}

	private static String getPath(String path) {
		File file = new File(path);
		try {
			if (!file.isFile()) {
				URL resolveConfig = environment.resolveConfig(path);
				if (path != null) {
					return resolveConfig.getPath();
				}
			}
		} catch (FailedToResolveConfigException e) {
			logger.error(path + " read error!");
		}
		return path;
	}

	private static void loadStopWords() throws IOException {
		Set<String> filters = new HashSet<String>();
		BufferedReader br = MyStaticValue.stopWordReader();
		String temp = null;
		while ((temp = br.readLine()) != null) {
			filters.add(temp.trim());
		}
		stopWordsSet = filters;
		logger.info("stop words loaded!");
	}

	private static void loadSynonymTree() throws IOException {
		File synonymLibrary = new File(synonymPath);
		if (!synonymLibrary.isFile() || !synonymLibrary.canRead()) {
			logger.info("Can't find file:" + synonymLibrary);
			synonymTree = null;
			return;
		} else {
			synonymTree = new TreeMap<String, List<String>>();
			InputStream in = new FileInputStream(synonymLibrary);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "UTF-8"));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				String[] strs = temp.split(",");
				List<String> list = new LinkedList<String>();
				for (int i = 0; i < strs.length; i++) {
					list.add(strs[i]);
				}
				for (int i = 0; i < strs.length; i++) {
					LinkedList<String> newList = new LinkedList<String>(list);
					newList.remove(i);
					List<String> oldList = synonymTree.get(strs[i]);
					if (oldList != null) {
						LinkedList<String> list3 = new LinkedList<String>();
						for (int j = 0; j < oldList.size(); j++) {
							if (newList.contains(oldList.get(j)))
								list3.add(oldList.get(j));
						}
						if (list3.size() > 0)
							synonymTree.put(strs[i], list3);
					} else
						synonymTree.put(strs[i], newList);
				}
			}
			logger.info("synonym dictionary loaded!");
			reader.close();
			in.close();
		}
	}

	private static void loadVectorTree() throws IOException {
	}
}
