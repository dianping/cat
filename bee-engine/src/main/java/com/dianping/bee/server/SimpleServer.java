package com.dianping.bee.server;

import java.io.IOException;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.model.SystemConfig;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.NIOAcceptor;
import com.alibaba.cobar.net.NIOConnector;
import com.alibaba.cobar.net.NIOProcessor;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.ContainerLoader;
import com.site.lookup.annotation.Inject;

public class SimpleServer implements LogEnabled {
	@Inject
	private int m_port = 2330;

	private Logger m_logger;

	/**
	 * The mysql version can not be changed, JDBC Driver will parse mysql major
	 * and minor version information
	 */
	public static final String VERSION = "5.1.48-bee-0.0.1";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public void startup() throws IOException {
		SystemConfig system = CobarServer.getInstance().getConfig().getSystem();
		FrontendConnection.setServerVersion(VERSION);
		// start processors
		NIOProcessor[] processors = new NIOProcessor[system.getProcessors()];

		for (int i = 0; i < processors.length; i++) {
			processors[i] = new NIOProcessor("Processor" + i, system.getProcessorHandler(), system.getProcessorExecutor());
			processors[i].startup();
		}

		// startup connector
		NIOConnector connector = new NIOConnector("BeeConnector");

		connector.setProcessors(processors);
		connector.start();

		// startup server
		SimpleServerConnectionFactory sf = new SimpleServerConnectionFactory();

		sf.setIdleTimeout(system.getIdleTimeout()); // one hour
		sf.setContainer(ContainerLoader.getDefaultContainer());

		NIOAcceptor server = new NIOAcceptor("BeeServer", m_port, sf);

		server.setProcessors(processors);
		server.start();

		Threads.forGroup("Bee").start(new ProcessorCheckTask(processors));

		m_logger.info(String.format("BEE server started at %s", m_port));
	}

	static class ProcessorCheckTask implements Task {
		private NIOProcessor[] m_processors;

		public ProcessorCheckTask(NIOProcessor[] processors) {
			m_processors = processors;
		}

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(1000);

					for (NIOProcessor processor : m_processors) {
						try {
							processor.check();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
