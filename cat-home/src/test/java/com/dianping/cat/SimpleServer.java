package com.dianping.cat;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class SimpleServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(2281);
		WebAppContext context = new WebAppContext();

		System.setProperty("devMode", "true");
		context.setContextPath("/cat");
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
		context.setResourceBase("src/main/webapp");
		server.setHandler(context);
		server.start();
		server.join();
	}
}
