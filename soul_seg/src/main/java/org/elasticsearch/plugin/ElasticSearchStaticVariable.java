package org.elasticsearch.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.soul.treeSplit.IOUtil;
import org.soul.utility.MyStaticValue;

public class ElasticSearchStaticVariable {
	private static Log log = LogFactory
			.getLog(ElasticSearchStaticVariable.class);
	public static ESLogger logger = Loggers.getLogger("soul-analyzer");
	public static boolean loaded = false;
	public static Set<String> filter;
	public static TreeMap<String, List<String>> synonymTree;
	public static boolean pstemming = false;
	public static Environment environment;

	static {
		Tuple<Settings, Environment> tuple = InternalSettingsPreparer
				.prepareSettings(Builder.EMPTY_SETTINGS, true);
		environment = tuple.v2();
		initConfigPath(tuple.v1());
		loadFilter(); // load stop words
		loadSynonymArray();
		ElasticSearchStaticVariable.loaded = true;
	}

	private static void initConfigPath(Settings settings) {
		// whether use stemming for English word
		pstemming = settings.getAsBoolean("soul_pstemming", false);
		// userLibrary
		MyStaticValue.userLibrary = getPath(settings.get("soul_userLib",
				MyStaticValue.userLibrary));
		// ambiguityLibrary
		MyStaticValue.ambiguityLibrary = getPath(settings.get(
				"soul_ambiguityLib", MyStaticValue.ambiguityLibrary));
		// stop word library
		MyStaticValue.stopLibrary = getPath(settings.get("soul_stopPath",
				MyStaticValue.stopLibrary));
		// log.info(StaticVarForSegment.stopLibrary);
		MyStaticValue.synonymLibrary = getPath(settings.get(
				"soul_synonymPath", MyStaticValue.synonymLibrary));
	}

	private static String getPath(String path) {
		log.info(path);
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

	private static void loadFilter() {
		File stopLibrary = new File(MyStaticValue.stopLibrary);
		if (!stopLibrary.isFile() || !stopLibrary.canRead()) {
			logger.info("Can't find file:" + MyStaticValue.stopLibrary
					+ "!");
			filter = new HashSet<String>();
			return;
		} else {
			Set<String> filters = new HashSet<String>();
			BufferedReader br;
			try {
				br = IOUtil.getReader(stopLibrary.getAbsolutePath(), "UTF-8");
				String temp = null;
				while ((temp = br.readLine()) != null) {
					filters.add(temp.trim());
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			filter = filters;
			logger.info("stop words loaded!");
		}
	}

	private static void loadSynonymArray() {
		File synonymLibrary = new File(MyStaticValue.synonymLibrary);
		if (!synonymLibrary.isFile() || !synonymLibrary.canRead()) {
			logger.info("Can't find file:" + MyStaticValue.synonymLibrary
					+ "!");
			synonymTree = null;
			return;
		} else {
			try {
				synonymTree = new TreeMap<String, List<String>>();
				InputStream in = new FileInputStream(synonymLibrary);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in, "UTF-8"));
				String temp = null;
				while ((temp = reader.readLine()) != null) {
					String[] strs = temp.split(",");
					List<String> list = new LinkedList<String>();
					for (int i = 0; i < strs.length; i++) {
						list.add(strs[i]);
					}
					for (int i = 0; i < strs.length; i++) {
						LinkedList<String> newList = new LinkedList<String>(
								list);
						newList.remove(i);
						List<String> oldList = synonymTree.get(strs[i]);
						if (oldList != null) {
							// get common set
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
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
