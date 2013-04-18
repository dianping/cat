package com.dianping.cat.report.page.pushError;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "pushError")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "pushError")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		long timestamp = payload.getTimestamp();
		String error = payload.getError();
		String file = payload.getFile();
		String line = payload.getLine();
		String url = payload.getUrl();
		String host = payload.getHost();

		Cat.logEvent("Error", file, "Error", error);
		Cat.logEvent("Error.url", url, Message.SUCCESS, "line=" + line + "&time="
		      + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp)));

		DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
		tree.setDomain("FrontEnd");
		tree.setHostName(String.valueOf(host));
		tree.setIpAddress(String.valueOf(host));

		model.setStatus("SUCCESS");
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.PUSHERROR);
		m_jspViewer.view(ctx, model);
	}
}
