package com.dianping.cat.servlet;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dianping.cat.Cat;

public class CatListener implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Cat.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		String catClientXml = ctx.getInitParameter("cat-client-xml");

		if (catClientXml == null) {
			catClientXml = "/data/appdatas/cat/client.xml";
		}
		
		Cat.initialize(new File(catClientXml));
		//for background thread
		Cat.setup(null,null);
	}
}
