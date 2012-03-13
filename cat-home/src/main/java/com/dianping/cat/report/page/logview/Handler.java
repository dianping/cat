package com.dianping.cat.report.page.logview;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.storage.TagThreadSupport.Direction;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "logview")
	private ModelService<String> m_service;

	private String getLogView(String messageId, Direction direction, String tag) {
		if (messageId != null) {
			MessageId id = MessageId.parse(messageId);
			ModelPeriod period = ModelPeriod.getByTime(id.getTimestamp());
			ModelRequest request = new ModelRequest(id.getDomain(), period) //
			      .setProperty("messageId", messageId);

			if (direction != null && tag != null) {
				request.setProperty("direction", direction.name());
				request.setProperty("tag", tag);
			}

			if (m_service.isEligable(request)) {
				ModelResponse<String> response = m_service.invoke(request);
				String logview = response.getModel();

				return logview;
			} else {
				throw new RuntimeException("Internal error: no eligible service registered for " + request + "!");
			}
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

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.LOGVIEW);

		Payload payload = ctx.getPayload();
		String messageId = getMessageId(payload);
		String logView = getLogView(messageId, payload.getDirection(), payload.getTag());

		model.setTable(logView);
		m_jspViewer.view(ctx, model);
	}
}
