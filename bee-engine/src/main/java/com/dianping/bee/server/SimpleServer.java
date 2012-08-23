package com.dianping.bee.server;

import java.io.IOException;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.alibaba.cobar.net.NIOAcceptor;
import com.alibaba.cobar.net.NIOConnector;
import com.alibaba.cobar.net.NIOProcessor;
import com.alibaba.cobar.server.ServerConnectionFactory;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class SimpleServer implements LogEnabled {
	@Inject
	private int m_port = 2330;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public void startup() throws IOException {
		NIOProcessor[] processors = new NIOProcessor[4];

		for (int i = 0; i < processors.length; i++) {
			processors[i] = new NIOProcessor("Processor" + i, 4, 4);
			processors[i].startup();
		}

		Threads.forGroup("Bee").start(new ProcessorCheckTask(processors));

		// startup connector
		NIOConnector connector = new NIOConnector("BeeConnector");
		connector.setProcessors(processors);
		connector.start();

		// startup server
		ServerConnectionFactory sf = new ServerConnectionFactory();

		sf.setIdleTimeout(3600 * 1000L); // one hour

		NIOAcceptor server = new NIOAcceptor("BeeServer", m_port, sf);

		server.setProcessors(processors);
		server.start();

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
