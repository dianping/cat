package com.dianping.cat.report.page.logview;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "logview")
	private ModelService<String> m_service;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private String getLogView(String messageId, boolean waterfall) {
		try {
			if (messageId != null) {
				MessageId id = MessageId.parse(messageId);
				ModelPeriod period = ModelPeriod.getByTime(id.getTimestamp());
				ModelRequest request = new ModelRequest(id.getDomain(), period) //
				      .setProperty("messageId", messageId) //
				      .setProperty("waterfall", String.valueOf(waterfall))
				      .setProperty("timestamp", String.valueOf(id.getTimestamp()));

				if (m_service.isEligable(request)) {
					ModelResponse<String> response = m_service.invoke(request);
					String logview = response.getModel();

					return logview;
				} else {
					throw new RuntimeException("Internal error: no eligible logview service registered for " + request + "!");
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}

		return null;
	}

	private String getMessageId(Payload payload) {
		String[] path = payload.getPath();

		if (path != null && path.length > 0) {
			return path[0];
		} else {
			return null;
		}
	}

	private String getPath(String messageId) {
		MessageId id = MessageId.parse(messageId);
		final String path = m_pathBuilder.getPath(new Date(id.getTimestamp()), "");
		final StringBuilder sb = new StringBuilder();
		sb.append('/').append(path);

		final String key = id.getDomain() + '-' + id.getIpAddress();
		return path + key;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "m")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "m")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.LOGVIEW);
		model.setDomain(payload.getDomain());
		model.setLongDate(payload.getDate());

		String messageId = getMessageId(payload);
		String logView = getLogView(messageId, payload.isWaterfall());

		if (logView == null || logView.length() == 0) {
			Cat.getProducer().logEvent("Logview", "Fail", Event.SUCCESS, null);
		} else {
			Cat.getProducer().logEvent("Logview", "Success", Event.SUCCESS, null);
		}
		
		switch (payload.getAction()) {
		case VIEW:
			model.setTable(logView);
			break;
		case MOBILE:
			model.setMobileResponse(logView);
			break;
		case DETAIL:
			String path = getPath(messageId);
			model.setLogviewPath(path);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

}
