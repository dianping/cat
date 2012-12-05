package com.dianping.cat.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
import com.dianping.cat.message.Message;
import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;

public class ClientConfigReloader implements Task {
	private static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";

	private ClientConfig m_config;

	private DefaultDomParser m_parser;

	private File m_file;

	private long m_lastModifyTime;

	private volatile boolean m_active = true;

	public ClientConfigReloader(String fileName, ClientConfig config) {
		m_config = config;
		m_parser = new DefaultDomParser();
		m_file = new File(fileName);
		m_lastModifyTime = m_file.lastModified();
	}

	@Override
	public String getName() {
		return "ClientConfigReloader";
	}

	private boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	public ClientConfig getClientConfig() throws IOException, SAXException {
		ClientConfig clientConfig = null;
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_CLIENT_XML);

		if (in == null) {
			in = Cat.class.getResourceAsStream(CAT_CLIENT_XML);
		}

		if (in != null) {
			String xml = Files.forIO().readFrom(in, "utf-8");

			clientConfig = new DefaultDomParser().parse(xml);
		}

		String content = Files.forIO().readFrom(m_file, "utf-8");
		ClientConfig globalConfig = m_parser.parse(content);

		if (globalConfig != null && clientConfig != null) {
			globalConfig.accept(new ClientConfigMerger(clientConfig));
		}

		return clientConfig;
	}

	@Override
	public void run() {
		while (isActive()) {
			try {
				try {
					long now = m_file.lastModified();

					if (now > m_lastModifyTime) {
						ClientConfig newConfig = getClientConfig();

						Map<String, Domain> domains = newConfig.getDomains();
						Domain firstDomain = domains.isEmpty() ? null : domains.values().iterator().next();

						boolean catEnable = firstDomain.getEnabled();
						boolean oldEnabled = m_config.isEnabled();

						if (oldEnabled != catEnable) {
							if (oldEnabled) {
								Cat.getProducer().logEvent("System", "Reload:" + catEnable, Message.SUCCESS,
								      String.format("Change from %s to %s", oldEnabled, catEnable));
							}
							synchronized (m_config) {
								m_config.setEnabled(catEnable);
							}
							if (catEnable) {
								Cat.getProducer().logEvent("System", "Reload:" + catEnable, Message.SUCCESS,
								      String.format("Change from %s to %s", oldEnabled, catEnable));
							}
						}
					}
				} catch (IOException e) {
					Cat.getProducer().logEvent("System", "ReloadIOException", "IOException", null);
				} catch (SAXParseException e) {
					Cat.getProducer().logEvent("System", "ReloadSAXException", "SAXException", null);
				} catch (RuntimeException e) {
					Cat.getProducer().logEvent("System", "ReloadException", "RuntimeException", null);
				} catch (Exception e) {
					Cat.logError(e);
				}
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				m_active = false;
			}
		}
	}

	@Override
	public void shutdown() {
		m_active = false;
	}
}