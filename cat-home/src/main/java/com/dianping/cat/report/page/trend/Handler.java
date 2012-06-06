package com.dianping.cat.report.page.trend;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "trend")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "trend")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TREND);
		copyProperties(payload, model);
		
		String graphType = payload.getGraphType();
		if (graphType == null || graphType.length() == 0) {
			graphType = "URL";
		}
		model.setGraphType(graphType);
		

		m_jspViewer.view(ctx, model);
	}

	private List<String> getTransactionDetails() {
		return null;
	}

	private List<String> getEventDetails() {
		return null;
	}

	private void copyProperties(Payload payload, Model model) {
		String queryIP = payload.getQueryIP();
		if (queryIP != null) {
			model.setQueryIP(payload.getQueryIP());
		}
		String queryType = payload.getQueryType();
		if (queryType != null) {
			model.setQueryType(payload.getQueryType());
		}
		String queryName = payload.getQueryName();
		if (queryName != null) {
			model.setQueryName(payload.getQueryName());
		}
		String dateType = payload.getDateType();
		if (dateType != null) {
			model.setDateType(payload.getDateType());
		}
		String queryDate = payload.getQueryDate();
		if (queryDate != null) {
			model.setQueryDate(queryDate);
		}
		String QueryOption = payload.getSelfQueryOption();
		if (QueryOption != null) {
			model.setSelfQueryOption(payload.getSelfQueryOption());
		}
	}
}
