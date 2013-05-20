package com.dianping.cat.report.page.externalError;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private EventCollectManager m_errorCollectManager;

	@Inject
	private DomainManager m_domainManager;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "externalError")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "externalError")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		int type = payload.getType();
		String ip = payload.getIp();
		String subject = payload.getTitle();
		String content = payload.getContent();
		String time = payload.getTime();
		Event event = new Event();

		if (StringUtils.isEmpty(subject)) {
			subject = content;
		}
		if (type == EventCollectManager.DB_ERROR) {
			event.setDomain(payload.getDatabase());
		} else {
			String domain = m_domainManager.getDomainByIp(ip);

			event.setDomain(domain);
		}
		event.setIp(ip);
		event.setType(type);
		event.setContent(content);
		event.setSubject(subject);
		event.setLink(payload.getLink());
		try {
			event.setDate(m_sdf.parse(time));
		} catch (ParseException e) {
			event.setDate(new Date());
			try {
				event.setDate(new Date(Integer.parseInt(time)));
			} catch (Exception e1) {
				event.setDate(new Date());
				Cat.logError(e1);
			}
		}
		m_errorCollectManager.addEvent(event);
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.EXTERNALERROR);
		m_jspViewer.view(ctx, model);
	}
}
