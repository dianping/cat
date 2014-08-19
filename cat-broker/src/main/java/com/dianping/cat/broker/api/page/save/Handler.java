package com.dianping.cat.broker.api.page.save;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.broker.api.app.AppDataConsumer;

public class Handler implements PageHandler<Context> {
	@Inject
	protected JspViewer m_jspViewer;
	
	@Inject
	private AppDataConsumer m_appDataConsumer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "save")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "save")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		HttpServletResponse response = ctx.getHttpServletResponse();
		
		m_appDataConsumer.save();
		
		response.getWriter().write("OK");
	}
}
