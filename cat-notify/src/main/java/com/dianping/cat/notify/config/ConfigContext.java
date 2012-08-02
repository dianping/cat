package com.dianping.cat.notify.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.dianping.cat.notify.util.StringUtil;

/**
 * 配置文件类，主要从config.properties加载配置信息
 */
public class ConfigContext {

	private int webServerPort = 8088;

	private int connectorMaxIdleTime = 30000;

	private int connectorRequestHeaderSize = 8192;

	private int connectorThreadPoolSize = 10;

	private final Properties config;

	public ConfigContext() {

		config = loadConfig("configuration", "config.properties");

		String webServerPortStr = config.getProperty("web.server.port");
		if (StringUtil.isNotBlank(webServerPortStr)) {
			webServerPort = Integer.valueOf(webServerPortStr);
		}

		String maxIdleTimeStr = config.getProperty("web.server.connector.maxIdleTime");
		if (StringUtil.isNotBlank(maxIdleTimeStr)) {
			connectorMaxIdleTime = Integer.valueOf(maxIdleTimeStr);
		}

		String requestHeaderSizeStr = config.getProperty("web.server.connector.requestHeaderSize");
		if (StringUtil.isNotBlank(requestHeaderSizeStr)) {
			connectorRequestHeaderSize = Integer.valueOf(requestHeaderSizeStr);
		}

		String threadPoolSizeStr = config.getProperty("web.server.connector.threadPoolSize");
		if (StringUtil.isNotBlank(threadPoolSizeStr)) {
			connectorThreadPoolSize = Integer.valueOf(threadPoolSizeStr);
		}
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

	public int getConnectorMaxIdleTime() {
		return connectorMaxIdleTime;
	}

	public int getConnectorRequestHeaderSize() {
		return connectorRequestHeaderSize;
	}

	public int getConnectorThreadPoolSize() {
		return connectorThreadPoolSize;
	}

	public int getWebServerPort() {
		return webServerPort;
	}

	public String getProperty(String key) {
		return config.getProperty(key);
	}
}
