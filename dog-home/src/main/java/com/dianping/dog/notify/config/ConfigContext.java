package com.dianping.dog.notify.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * 配置文件类，主要从config.properties加载配置信息
 */
public class ConfigContext {

	private final Properties config;

	public ConfigContext() {
		config = loadConfig("configuration", "config.properties");
	}

	public static Properties loadConfig(String configKey, String defFileName) {
		String configFile = System.getProperty(configKey, defFileName);

		InputStream stream = null;
		File cfg = new File(configFile);
		Properties config = new Properties();
		try {
			stream = new FileInputStream(cfg);
		} catch (FileNotFoundException e) {
			stream = ConfigContext.class.getClassLoader().getResourceAsStream(configFile);
		} finally {
			if (stream != null) {
				try {
					config.load(stream);
				} catch (IOException e) {
					throw new RuntimeException("don't load config file! path = " + configFile, e);
				} finally {
					IOUtils.closeQuietly(stream);
				}
			} else {
				throw new RuntimeException("the file is not existed! path = " + configFile);
			}
		}
		return config;
	}

	public String getProperty(String key) {
		return config.getProperty(key);
	}
}
