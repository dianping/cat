package com.dianping.cat.notify.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.notify.render.IRender;
import com.dianping.cat.notify.util.StringUtil;

public class MainServlet extends HttpServlet {
	private final static Logger logger = LoggerFactory.getLogger(MainServlet.class);

	private static final long serialVersionUID = 1L;

	private IRender render;

	private Handler handler;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String processorKey = req.getParameter("action");
		if (StringUtil.isEmpty(processorKey)) {
			printErrorPage(resp);
			return;
		}
		Request request = new Request(req);
		request.setProcessorKey(processorKey);
		Response response = handler.process(request);
		if (null == response || StringUtil.isEmpty(response.getTemplate())) {
			printErrorPage(resp);
			return;
		}
		if (response.getOutputType() != null) {
			resp.setContentType(response.getOutputType());
		}
		String result = render.fetchAll(response.getTemplate(), response.getParams());
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");
		if (result == null) {
			writer.write("error nothing display!");
			return;
		}
		writer.write(result);
	}

	private void printErrorPage(HttpServletResponse resp) {
		resp.setContentType("text/html");
		try {
			PrintWriter writer = resp.getWriter();
			writer.write("error nothing display!");
		} catch (IOException e) {
			logger.error("fail to print error page:" + e.toString());
		}
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	public void setRender(IRender render) {
		this.render = render;
	}

	public void setHandler(StandardHandler handler) {
		this.handler = handler;
	}

}
