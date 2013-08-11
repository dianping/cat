package com.dianping.cat.report.page.bug;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ReportService;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private ProductLineConfigManager m_configManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "bug")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "bug")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setPage(ReportPage.BUG);
		m_jspViewer.view(ctx, model);
	}

	private BugReport queryBugReport(Payload payload) {
		Date start = null;
		Date end = null;
		if (payload.getAction() == Action.HOURLY_REPORT) {
			start = new Date(payload.getDate());
			end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		} else {
			start = payload.getHistoryStartDate();
			end = payload.getHistoryEndDate();
		}

		return m_reportService.queryBugReport(CatString.CAT, start, end);
	}
}
