package com.dianping.cat.report.page.database;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

/**
 * @author youyong
 */
public class Handler implements PageHandler<Context> {

	@Inject
	private ReportService m_reportService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "database")
	private ModelService<DatabaseReport> m_service;

	private DatabaseReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("database", payload.getDatabase());

		if (m_service.isEligable(request)) {
			ModelResponse<DatabaseReport> response = m_service.invoke(request);
			DatabaseReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDatabaseNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_DAY), "database");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable database service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "database")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "database")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		String domain = payload.getDomain();

		switch (payload.getAction()) {
		case HISTORY_REPORT:
			DatabaseReport historyReport = showSummarizeReport(model, payload);

			long historyDuration = historyReport.getEndTime().getTime() - historyReport.getStartTime().getTime();
			DisplayDatabase displayHistoryDatabase = new DisplayDatabase().setDomain(domain).setDuration(historyDuration);

			displayHistoryDatabase.setSortBy(payload.getSortBy()).visitDatabaseReport(historyReport);
			model.setReport(historyReport);
			model.setDisplayDatabase(displayHistoryDatabase);
			break;
		case HOURLY_REPORT:
			long hourlyDuration = TimeUtil.ONE_HOUR;
			if (ModelPeriod.CURRENT == payload.getPeriod()) {
				hourlyDuration = System.currentTimeMillis() % TimeUtil.ONE_HOUR;
			}
			DatabaseReport hourlyReport = getHourlyReport(payload);
			DisplayDatabase displayDatabase = new DisplayDatabase().setDomain(domain).setDuration(hourlyDuration);

			displayDatabase.setSortBy(payload.getSortBy()).visitDatabaseReport(hourlyReport);
			model.setReport(hourlyReport);
			model.setDisplayDatabase(displayDatabase);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	public void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.DATABASE);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain("All");
		}
		if (StringUtils.isEmpty(payload.getDatabase())) {
			payload.setDatabase("cat");
		}
		model.setDatabase(payload.getDatabase());
		if (!payload.getDomain().equals("All")) {
			model.setDisplayDomain(payload.getDomain());
			model.setDomain(payload.getDomain());
		}
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

	private DatabaseReport showSummarizeReport(Model model, Payload payload) {
		String database = payload.getDatabase();

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryDatabaseReport(database, start, end);
	}
}
