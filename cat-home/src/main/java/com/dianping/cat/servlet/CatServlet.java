package com.dianping.cat.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.transform.DefaultXmlParser;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.site.helper.Files;
import com.site.web.AbstractContainerServlet;

public class CatServlet extends AbstractContainerServlet {
	private static final long serialVersionUID = 1L;

	private static final String CAT_SERVER_XML = "/META-INF/cat/server.xml";

	private Exception m_exception;

	@Override
	protected void initComponents(ServletConfig servletConfig) throws ServletException {
		String catServerXml = servletConfig.getInitParameter("cat-server-xml");
		ClientConfig config = loadConfig(null);

		try {
			MessageManager manager = lookup(MessageManager.class);

			manager.initializeServer(config);

			ServerConfigManager configManager = lookup(ServerConfigManager.class);

			configManager.initialize(new File(catServerXml));

			final DefaultMessageHandler handler = (DefaultMessageHandler) lookup(MessageHandler.class);

			new Thread(handler).start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					handler.shutdown();
				}
			});
		} catch (Exception e) {
			m_exception = e;
			throw new RuntimeException("Error when initializing CatServlet, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}

	protected ClientConfig loadConfig(String configFile) {
		ClientConfig config = null;

		// read config from local file system
		try {
			if (configFile != null) {
				String xml = Files.forIO().readFrom(new File(configFile).getCanonicalFile(), "utf-8");

				config = new DefaultXmlParser().parse(xml);
			}

			if (config == null) {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CAT_SERVER_XML);

				if (in == null) {
					in = Cat.class.getResourceAsStream(CAT_SERVER_XML);
				}

				if (in != null) {
					String xml = Files.forIO().readFrom(in, "utf-8");

					config = new DefaultXmlParser().parse(xml);
				}
			}
		} catch (Exception e) {
			m_exception = e;
			throw new RuntimeException(String.format("Error when loading configuration file: %s!", configFile), e);
		}

		return config;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/plain");

		PrintWriter writer = res.getWriter();

		if (m_exception != null) {
			writer.write("Server has NOT been initialized successfully! \r\n\r\n");
			m_exception.printStackTrace(writer);
		} else {
			writer.write("Not implemented yet!");
		}
	}
}
