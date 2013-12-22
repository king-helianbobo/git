package org.soul.elasticSearch.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.soul.splitWord.BasicAnalysis;
import org.soul.treeSplit.IOUtil;
import org.soul.util.StaticVariable;

public class SoulElasticStaticValue {
	public static ESLogger logger = Loggers.getLogger("soul-analyzer");
	private static boolean loaded = false;
	public static Set<String> filter;
	public static boolean pstemming = false;
	public static Environment environment;

	static {
		init();
	}

	private static void init() {
		Tuple<Settings, Environment> tuple = InternalSettingsPreparer
				.prepareSettings(Builder.EMPTY_SETTINGS, true);
		environment = tuple.v2();
		initConfigPath(tuple.v1());
		loadFilter(tuple.v1()); // 加载停用词表
		// preheat();
		setLoaded(true);
	}

	// private static void preheat() {
	// BasicAnalysis.parse("一个词");
	// }

	private static void initConfigPath(Settings settings) {
		// 是否提取词干
		pstemming = settings.getAsBoolean("soul_pstemming", false);
		// 用户自定义辞典
		StaticVariable.userLibrary = getPath(settings.get("soul_user_path",
				StaticVariable.userLibrary));
		// 用户自定义辞典
		StaticVariable.ambiguityLibrary = getPath(settings.get(
				"soul_ambiguity", StaticVariable.ambiguityLibrary));

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

	private static void loadFilter(Settings settings) {
		Set<String> filters = new HashSet<String>();
		String stopLibraryPath = settings.get("stop_path");
		if (stopLibraryPath == null) {
			return;
		}

		File stopLibrary = new File(environment.configFile(), stopLibraryPath);
		if (!stopLibrary.isFile()) {
			logger.info("Can't find file:" + stopLibraryPath
					+ ", no such file or directory exists!");
			emptyFilter();
			setLoaded(true);
			return;
		}

		BufferedReader br;
		try {
			br = IOUtil.getReader(stopLibrary.getAbsolutePath(), "UTF-8");
			String temp = null;
			while ((temp = br.readLine()) != null) {
				filters.add(temp);
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

	private static void emptyFilter() {
		filter = new HashSet<String>();
	}

	public static boolean isLoaded() {
		return loaded;
	}

	public static void setLoaded(boolean loaded) {
		SoulElasticStaticValue.loaded = loaded;
	}

	/**
	 * 重新加载配置文件
	 */
	public void reload() {
		init();
	}
}