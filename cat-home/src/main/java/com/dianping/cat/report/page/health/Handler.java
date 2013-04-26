package com.dianping.cat.report.page.health;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.service.ReportService;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private NormalizePayload m_normalizePayload;
	
	@Inject
	private ReportService m_reportService;

	@Inject
	private HistoryGraphs m_graphs;

	private long getCurrentDate(Payload payload) {
		long date = payload.getDate();
		long lastHour = payload.getCurrentDate() - TimeUtil.ONE_HOUR;
		long lastTwoHour = payload.getCurrentDate() - 2 * TimeUtil.ONE_HOUR;
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);

		if (minute > 15) {
			if (date > lastHour) {
				date = lastHour;
			}
		} else {
			if (date >= lastTwoHour) {
				date = lastTwoHour;
			}
		}
		return date;
	}

	private HealthReport getHistoryReport(Date startDate, Date endDate, String domain) {
		try {
			return m_reportService.queryHealthReport(domain, startDate, endDate);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	private HealthReport getHourlyReport(long date, String domain) {
		try {
			return m_reportService.queryHealthReport(domain, new Date(date), new Date(date + TimeUtil.ONE_HOUR));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	private long getLastDate(long current, String reportType) {
		if (reportType.equalsIgnoreCase("day")) {
			current = current - TimeUtil.ONE_HOUR * 24;
		} else if (reportType.equalsIgnoreCase("week")) {
			current = current - TimeUtil.ONE_HOUR * 24 * 7;
		} else if (reportType.equalsIgnoreCase("month")) {
			Calendar cal = Calendar.getInstance();

			cal.setTime(new Date(current));
			cal.add(Calendar.MONTH, -1);
			return cal.getTimeInMillis();
		}
		return current;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "health")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "health")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);

		switch (payload.getAction()) {
		case HOURLY_REPORT:
			long currentDate = getCurrentDate(payload);
			HealthReport report = getHourlyReport(currentDate, payload.getDomain());
			HealthReport lastReport = getHourlyReport(currentDate - TimeUtil.ONE_HOUR, payload.getDomain());
			HealthReport lastTwoReport = getHourlyReport(currentDate - TimeUtil.ONE_HOUR * 2, payload.getDomain());

			model.setReport(report);
			model.setLastReport(lastReport);
			model.setLastTwoReport(lastTwoReport);
			break;
		case HISTORY_REPORT:
			Date historyStartDate = payload.getHistoryStartDate();
			Date historyEndDate = payload.getHistoryEndDate();

			long current = historyStartDate.getTime();
			long currentEnd = historyEndDate.getTime();
			long lastStart = getLastDate(current, payload.getReportType());
			long lastEnd = getLastDate(currentEnd, payload.getReportType());
			long lastTwoStart = getLastDate(lastStart, payload.getReportType());
			long lastTwoEnd = getLastDate(lastEnd, payload.getReportType());

			HealthReport historyReport = getHistoryReport(historyStartDate, historyEndDate, payload.getDomain());
			HealthReport historyLastReport = getHistoryReport(new Date(lastStart), new Date(lastEnd), payload.getDomain());
			HealthReport historyLastTwoReport = getHistoryReport(new Date(lastTwoStart), new Date(lastTwoEnd),
			      payload.getDomain());
			model.setReport(historyReport);
			model.setLastReport(historyLastReport);
			model.setLastTwoReport(historyLastTwoReport);
			break;

		case HISTORY_GRAPH:
			Date graphStartDate = payload.getHistoryStartDate();
			Date graphEndDate = payload.getHistoryEndDate();
			String key = payload.getKey();

			HistoryGraphItem item = m_graphs.buildHistoryGraph(model.getDomain(), graphStartDate, graphEndDate,
			      payload.getReportType(), key);
			Gson gson = new Gson();
			model.setHistoryGraph(gson.toJson(item));
			break;
		}
		m_jspViewer.view(ctx, model);
	}
	
	private void normalize(Model model,Payload payload){
		model.setPage(ReportPage.HEALTH);
		m_normalizePayload.normalize(model, payload);
	}

}
