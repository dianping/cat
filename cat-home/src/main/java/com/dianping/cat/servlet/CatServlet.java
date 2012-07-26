		package com.dianping.cat.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

<<<<<<< HEAD
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.transform.DefaultDomParser;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.dianping.cat.report.page.ip.location.IPSeekerManager;
import com.dianping.cat.report.task.DailyTaskProducer;
import com.dianping.cat.report.task.TaskConsumer;
import com.site.helper.Files;
import com.site.helper.Threads;
import com.site.helper.Threads.DefaultThreadListener;
=======
import com.site.initialization.DefaultModuleContext;
import com.site.initialization.ModuleContext;
import com.site.initialization.ModuleInitializer;
>>>>>>> 34aa1347a27cbd2b5539cbcf2043a0c7acc392b2
import com.site.web.AbstractContainerServlet;

public class CatServlet extends AbstractContainerServlet {
	private static final long serialVersionUID = 1L;

	private Exception m_exception;

	@Override
	protected void initComponents(ServletConfig servletConfig) throws ServletException {
		try {
			ModuleContext ctx = new DefaultModuleContext(getContainer());
			ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);
			String catServerXml = servletConfig.getInitParameter("cat-server-xml");

<<<<<<< HEAD
			final DefaultMessageHandler handler = (DefaultMessageHandler) lookup(MessageHandler.class);

			Threads.forGroup("Cat").start(handler);
			
			Threads.forGroup("Cat").start(lookup(TaskConsumer.class));
			
			Threads.forGroup("Cat").start(lookup(DailyTaskProducer.class));
			
			
		} catch (Exception e) {
			m_exception = e;
			throw new RuntimeException("Error when initializing CatServlet, " + "please make sure the environment was setup correctly!", e);
		}
	}

	protected ClientConfig loadConfig(String configFile) {
		ClientConfig config = null;

		// read config from local file system
		try {
			if (configFile != null) {
				String xml = Files.forIO().readFrom(new File(configFile).getCanonicalFile(), "utf-8");
=======
			ctx.setAttribute("cat-client-config-file", new File("/data/appdatas/cat/client.xml"));
>>>>>>> 34aa1347a27cbd2b5539cbcf2043a0c7acc392b2

			if (catServerXml != null) {
				ctx.setAttribute("cat-server-config-file", new File(catServerXml));
			} else {
				ctx.setAttribute("cat-server-config-file", new File("/data/appdatas/cat/server.xml"));
			}

			initializer.execute(ctx);
		} catch (Exception e) {
			m_exception = e;
		}
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
