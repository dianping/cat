package com.dianping.cat.servlet;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

import com.dianping.cat.Cat;

public class CatServerListener implements ServletContextListener {
	private PlexusContainer m_container;

	private Logger m_logger;

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		m_container.dispose();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			m_container = ContainerLoader.getDefaultContainer();
			m_logger = ((DefaultPlexusContainer) m_container).getLoggerManager().getLoggerForComponent(
			      getClass().getName());

			ModuleContext ctx = new DefaultModuleContext(m_container);
			ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);
			File clientXmlFile = new File(Cat.getCatHome(), "client.xml");
			File serverXmlFile = new File(Cat.getCatHome(), "server.xml");

			ctx.setAttribute("cat-client-config-file", clientXmlFile);
			ctx.setAttribute("cat-server-config-file", serverXmlFile);
			initializer.execute(ctx);
		} catch (Exception e) {
			if (m_logger != null) {
				m_logger.error("Cat initializing failed. " + e, e);
			} else {
				System.out.println("Cat initializing failed. " + e);
				e.printStackTrace(System.out);
			}
		}
	}
}
