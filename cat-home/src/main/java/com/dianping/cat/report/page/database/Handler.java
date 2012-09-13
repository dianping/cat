package com.dianping.cat.report.page.database;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.database.DatabaseReportMerger;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.database.DatabaseMerger;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

/**
 * @author youyong
 */
public class Handler implements PageHandler<Context> {

	public static final long ONE_HOUR = 3600 * 1000L;

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	private DatabaseMerger m_databaseMerger;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

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
			long hourlyDuration = ONE_HOUR;
			if (ModelPeriod.CURRENT == payload.getPeriod()) {
				hourlyDuration = System.currentTimeMillis() % ONE_HOUR;
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
		model.setDisplayDomain(payload.getDomain());
		model.setDomain(payload.getDomain());

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

		DatabaseReport databaseReport = null;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findDatabaseAllByDomainNameDuration(start, end, database, "database",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findDatabaseAllByDomainNameDuration(start, end, null, "database",
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> databases = new HashSet<String>();
				for (Report report : allReports) {
					databases.add(report.getDomain());
				}
				databaseReport = m_databaseMerger.mergeForDaily(database, reports, databases);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} else {
			try {
				List<Dailyreport> reports = m_dailyreportDao.findDatabaseAllByDomainNameDuration(start, end, database,
				      "database", DailyreportEntity.READSET_FULL);
				DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(database));
				for (Dailyreport report : reports) {
					String xml = report.getContent();
					DatabaseReport reportModel = DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				databaseReport = merger.getDatabaseReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		databaseReport.setStartTime(start);
		databaseReport.setEndTime(end);

		return databaseReport;
	}
}
