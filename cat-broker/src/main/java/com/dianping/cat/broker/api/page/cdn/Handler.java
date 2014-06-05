package com.dianping.cat.broker.api.page.cdn;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cdn")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cdn")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		HttpServletResponse response = ctx.getHttpServletResponse();
		response.getWriter().write("OK");
	}
}
