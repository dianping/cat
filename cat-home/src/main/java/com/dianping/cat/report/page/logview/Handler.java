package com.dianping.cat.report.page.logview;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.webres.helper.Joiners;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
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

		model.setTable(getLogView(payload));
		m_jspViewer.view(ctx, model);
	}

	private String getLogView(Payload payload) {
		String[] path = payload.getPath();

		if (path != null && path.length > 0) {
			String file = path[0];
			int pos = file.lastIndexOf('.');
			MessageId id = MessageId.parse(pos < 0 ? file : file.substring(0, pos));
			String relativePath = Joiners.by('/').join(path);
			ModelPeriod period = ModelPeriod.getByTime(id.getTimestamp());
			ModelRequest request = new ModelRequest(id.getDomain(), period) //
			      .setProperty("path", relativePath);

			if (m_service.isEligable(request)) {
				ModelResponse<String> response = m_service.invoke(request);
				String logview = response.getModel();

				return logview;
			} else {
				throw new RuntimeException("Internal error: no eligable service registered for " + request + "!");
			}
		}

		return null;
	}
}
