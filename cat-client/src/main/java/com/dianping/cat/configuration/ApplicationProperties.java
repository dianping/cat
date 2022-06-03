package com.dianping.cat.configuration;

import static com.dianping.cat.CatClientConstants.APP_PROPERTIES;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.Logger;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;

// Component
public class ApplicationProperties implements Initializable, LogEnabled {
	private Properties m_properties = new Properties();

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String property = getProperty(key, null);

		if (property != null) {
			try {
				return Boolean.valueOf(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	public int getIntProperty(String key, int defaultValue) {
		String property = getProperty(key, null);

		if (property != null) {
			try {
				return Integer.parseInt(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	public long getLongProperty(String key, long defaultValue) {
		String property = getProperty(key, null);

		if (property != null) {
			try {
				return Long.parseLong(property);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}

		return defaultValue;
	}

	public String getProperty(String key, String defaultValue) {
		String property = m_properties.getProperty(key);

		if (property != null) {
			return property;
		}

		return defaultValue;
	}

	@Override
	public void initialize(ComponentContext ctx) {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES);

		if (in == null) {
			in = getClass().getClassLoader().getResourceAsStream(APP_PROPERTIES);
		}

		if (in != null) {
			try {
				m_properties.load(in);
			} catch (IOException e) {
				m_logger.warn("Resource(%s) is NOT found! IGNORED", APP_PROPERTIES);
			}
		}
	}
}
