package org.elasticsearch.hadoop.cfg;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

/**
 * Factory for loading settings based on various configuration objects, such as
 * Properties or Hadoop configuration. The factory main role is to minimize the
 * number of dependencies required at compilation time
 */
public abstract class SettingsManager {

	private final static Class<?> HADOOP_CONFIGURATION;

	static {
		Class<?> cfgClass = null;
		try {
			cfgClass = Class.forName("org.apache.hadoop.conf.Configuration",
					false, SettingsManager.class.getClassLoader());
		} catch (Exception ex) {
			// ignore
		}
		HADOOP_CONFIGURATION = cfgClass;
	}

	private abstract static class FromHadoopConfiguration {
		public static Settings create(Object cfg) {
			return new HadoopSettings((Configuration) cfg);
		}
	}

	public static Settings loadFrom(Object configuration) {
		if (configuration instanceof Properties) {
			return new PropertiesSettings((Properties) configuration);
		}
		if (HADOOP_CONFIGURATION != null
				&& HADOOP_CONFIGURATION.isInstance(configuration)) {
			return FromHadoopConfiguration.create(configuration);
		}
		throw new IllegalArgumentException(
				"Don't know how to create Settings from configuration "
						+ configuration);
	}
}