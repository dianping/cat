package com.dianping.cat.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

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
import com.site.helper.Threads;
import com.site.helper.Threads.DefaultThreadListener;
import com.site.web.AbstractContainerServlet;

public class CatServlet extends AbstractContainerServlet {
	private static final long serialVersionUID = 1L;

	private static final String CAT_SERVER_XML = "/META-INF/cat/server.xml";

	private Exception m_exception;

	@Override
	protected void initComponents(ServletConfig servletConfig) throws ServletException {
		String catServerXml = servletConfig.getInitParameter("cat-server-xml");
		ClientConfig config = loadConfig(null);

		Threads.addListener(new DefaultThreadListener() {
			@Override
			public void onThreadGroupCreated(ThreadGroup group, String name) {
				getLogger().info(String.format("Thread group(%s) created.", name));
			}
			
			@Override
			public void onThreadPoolCreated(ExecutorService pool, String name) {
				getLogger().info(String.format("Thread pool(%s) created.", name));
			}

			@Override
			public void onThreadStarting(Thread thread, String name) {
				getLogger().info(String.format("Starting thread(%s) ...", name));
			}

			@Override
			public void onThreadStopping(Thread thread, String name) {
				getLogger().info(String.format("Stopping thread(%s).", name));
			}

			@Override
			public boolean onUncaughtException(Thread thread, Throwable e) {
				getLogger().error(String.format("Uncaught exception thrown out of thread(%s)", thread.getName()), e);
				return true;
			}
		});

		// to notify CAT client to not add another ThreadListener
		getContainer().addContextValue("Cat.ThreadListener", "true");

		try {
			MessageManager manager = lookup(MessageManager.class);

			manager.initializeServer(config);

			ServerConfigManager serverConfigManager = lookup(ServerConfigManager.class);

			serverConfigManager.initialize(catServerXml == null ? null : new File(catServerXml));

			final DefaultMessageHandler handler = (DefaultMessageHandler) lookup(MessageHandler.class);

			Threads.forGroup().start(handler);
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
