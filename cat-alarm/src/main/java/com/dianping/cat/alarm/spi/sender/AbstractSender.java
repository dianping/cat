package com.dianping.cat.alarm.spi.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;

public abstract class AbstractSender implements Sender, LogEnabled {

	@Inject
	protected SenderConfigManager m_senderConfigManager;

	protected Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private boolean httpGetSend(String successCode, String urlPrefix, String urlPars) {
		URL url = null;
		InputStream in = null;
		URLConnection conn = null;

		try {
			url = new URL(urlPrefix + "?" + urlPars);
			conn = url.openConnection();

			conn.setConnectTimeout(2000);
			conn.setReadTimeout(3000);

			in = conn.getInputStream();
			StringBuilder sb = new StringBuilder();
			sb.append(Files.forIO().readFrom(in, "utf-8")).append("");

			if (sb.toString().contains(successCode)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private boolean httpPostSend(String successCode, String urlPrefix, String content) {
		URL url = null;
		InputStream in = null;
		OutputStreamWriter writer = null;
		URLConnection conn = null;

		try {
			url = new URL(urlPrefix);
			conn = url.openConnection();

			conn.setConnectTimeout(2000);
			conn.setReadTimeout(3000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
			writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(content);
			writer.flush();

			in = conn.getInputStream();
			StringBuilder sb = new StringBuilder();

			sb.append(Files.forIO().readFrom(in, "utf-8")).append("");
			if (sb.toString().contains(successCode)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public boolean httpSend(String successCode, String type, String urlPrefix, String urlPars) {
		if ("get".equalsIgnoreCase(type)) {
			return httpGetSend(successCode, urlPrefix, urlPars);
		} else if ("post".equalsIgnoreCase(type)) {
			return httpPostSend(successCode, urlPrefix, urlPars);
		} else {
			Cat.logError(new RuntimeException("Illegal request type: " + type));
			return false;
		}
	}

	public com.dianping.cat.alarm.sender.entity.Sender querySender() {
		String id = getId();

		return m_senderConfigManager.querySender(id);
	}
}
