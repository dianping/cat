package com.dianping.cat.agent.monitor;

import java.io.File;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class CatAgent {

	private final static Logger logger = Logger.getLogger(CatAgent.class);

	public static void addTerminateSingalHandler(final Server server) {
		// not officially supported API
		sun.misc.Signal.handle(new sun.misc.Signal("TERM"), new sun.misc.SignalHandler() {
			@Override
			public void handle(sun.misc.Signal signal) {
				logger.info(String.format("%s signal received, try to stop jetty server", signal));
				try {
					server.stop();
				} catch (Exception e) {
					logger.error("error stop jetty server", e);
				}
			}
		});
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 3) {
			logger.error("usage: port contextPath warRoot");
			return;
		}

		int port = Integer.parseInt(args[0]);
		String contextPath = args[1];
		File warRoot = new File(args[2]);

		logger.info(String.format("starting jetty@%d, contextPath %s, warRoot %s", port, contextPath, warRoot
		      .getAbsoluteFile().getAbsolutePath()));

		Server server = new Server(port);
		addTerminateSingalHandler(server);
		WebAppContext context = new WebAppContext();

		File jettyTmpDir = new File("/data/webapps/cat/jsp-work/");
		if (!jettyTmpDir.exists()) {
			if (!jettyTmpDir.mkdirs()) {
				throw new RuntimeException("Can not create jetty tmp dir at " + jettyTmpDir.getAbsolutePath());
			}
		}
		context.setContextPath(contextPath);
		context.setDescriptor(new File(warRoot, "WEB-INF/web.xml").getPath());
		context.setTempDirectory(jettyTmpDir);
		context.setResourceBase(warRoot.getPath());

		server.setHandler(context);
		server.start();

	}

}