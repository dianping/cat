package com.dianping.cat.report.page.health;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.health.HealthReportMerger;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	public static final long ONE_HOUR = 3600 * 1000L;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DailyreportDao m_dailyReportDao;

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
			HealthReport report = getHourlyReport(getLastDate(payload), payload.getDomain());
			model.setReport(report);
			break;
		case HISTORY_REPORT:
			HealthReport historyReport = getHistoryReport(payload.getHistoryStartDate(), payload.getHistoryEndDate(),
			      payload.getDomain());
			model.setReport(historyReport);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private long getLastDate(Payload payload) {
		long date = payload.getDate();
		long lastHour = payload.getCurrentDate() - ONE_HOUR;
		long lastTwoHour = payload.getCurrentDate() - 2 * ONE_HOUR;
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
			List<Dailyreport> reports = m_dailyReportDao.findAllByDomainNameDuration(startDate, endDate, domain, "health",
			      DailyreportEntity.READSET_FULL);
			HealthReportMerger merger = new HealthReportMerger(new HealthReport(domain));
			HealthReport healthReport = merger.getHealthReport();
			merger.setDuration(endDate.getTime() - startDate.getTime());

			for (Dailyreport report : reports) {
				String xml = report.getContent();
				HealthReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
				healthReport.getDomainNames().addAll(model.getDomainNames());
			}
			return healthReport;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	private HealthReport getHourlyReport(long date, String domain) {
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(new Date(date), domain, "health",
			      ReportEntity.READSET_FULL);

			HealthReportMerger merger = new HealthReportMerger(new HealthReport(domain));
			HealthReport healthReport = merger.getHealthReport();

			merger.setDuration(ONE_HOUR);

			for (Report report : reports) {
				String xml = report.getContent();
				HealthReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
				healthReport.getDomainNames().addAll(model.getDomainNames());
			}

			return healthReport;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	public void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.HEALTH);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		model.setDisplayDomain(payload.getDomain());

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		if (action == Action.HISTORY_REPORT) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			if (!payload.isToday()) {
				payload.setYesterdayDefault();
			}
			model.setLongDate(payload.getDate());
			model.setCustomDate(payload.getHistoryStartDate(), payload.getHistoryEndDate());
		}
	}
}
