package com.dianping.cat.notify.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.notify.config.ConfigContext;

public class WebServer {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ConfigContext configContext;

	private MainServlet mainServlet;

	public void init() {

		Server server = new Server();

		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(configContext.getWebServerPort());
		connector.setMaxIdleTime(configContext.getConnectorMaxIdleTime());
		connector.setRequestHeaderSize(configContext.getConnectorRequestHeaderSize());
		QueuedThreadPool threadPool = new QueuedThreadPool(configContext.getConnectorThreadPoolSize());
		threadPool.setName("cat-mail-jetty-server");
		connector.setThreadPool(threadPool);
		server.setConnectors(new Connector[] { connector });

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase("webapp");
		server.setHandler(context);

		context.addServlet(new ServletHolder(new DefaultServlet()), "/");
		addServlet(context);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("start web werver is failure", e);
		}
	}

	private void addServlet(ServletContextHandler context) {
		context.addServlet(new ServletHolder(mainServlet), "/do");
	}

	public void setConfigContext(ConfigContext config) {
		this.configContext = config;
	}

	public void setMainServlet(MainServlet mainServlet) {
		this.mainServlet = mainServlet;
	}
}
