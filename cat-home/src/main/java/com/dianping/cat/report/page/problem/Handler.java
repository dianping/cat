package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.problem.ProblemMerger;
import com.google.gson.Gson;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	private static final String DETAIL = "detail";

	private static final String VIEW = "view";

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProblemMerger m_problemMerger;

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	private ServerConfigManager m_manager;

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_service;

	private Gson m_gson = new Gson();

	private int getHour(long date) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private ProblemReport getHourlyReport(Payload payload, String type) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date).setProperty("type", type);
		if (!CatString.ALL_IP.equals(payload.getIpAddress())) {
			request.setProperty("ip", payload.getIpAddress());
		}
		if (!StringUtils.isEmpty(payload.getThreadId())) {
			request.setProperty("thread", payload.getThreadId());
		}
		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	private String getIpAddress(ProblemReport report, Payload payload) {
		Map<String, Machine> machines = report.getMachines();
		String ip = payload.getIpAddress();

		if ((ip == null || ip.length() == 0) && !machines.isEmpty()) {
			ip = machines.keySet().iterator().next();
		}

		return ip;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "p")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "p")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		ProblemReport report = null;
		ProblemStatistics problemStatistics = new ProblemStatistics();
		String ip = model.getIpAddress();
		int urlThreshold = payload.getLongTime();
		int sqlThreshold = payload.getSqlLongTime();

		switch (payload.getAction()) {
		case VIEW:
			report = getHourlyReport(payload, VIEW);
			model.setReport(report);
			if (ip.equals(CatString.ALL_IP)) {
				problemStatistics.setAllIp(true);
			} else {
				problemStatistics.setIp(ip);
			}
			problemStatistics.setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold);
			problemStatistics.visitProblemReport(report);
			model.setAllStatistics(problemStatistics);
			break;
		case HISTORY:
			report = showSummarizeReport(model, payload);
			if (ip.equals(CatString.ALL_IP)) {
				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold);
				problemStatistics.visitProblemReport(report);
			} else {
				problemStatistics.setIp(ip).setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold);
				problemStatistics.visitProblemReport(report);
			}
			model.setReport(report);
			model.setAllStatistics(problemStatistics);
			break;
		case HISTORY_GRAPH:
			m_historyGraphs.buildTrendGraph(model, payload);
			break;
		case GROUP:
			report = showHourlyReport(model, payload);
			if (report != null) {
				model.setGroupLevelInfo(new GroupLevelInfo(model).display(report));
			}
			break;
		case THREAD:
			report = showHourlyReport(model, payload);
			String groupName = payload.getGroupName();
			model.setGroupName(groupName);
			if (report != null) {
				model.setThreadLevelInfo(new ThreadLevelInfo(model, groupName).display(report));
			}
			break;
		case DETAIL:
			showDetail(model, payload);
			break;
		case MOBILE:
			if (ip.equals(CatString.ALL_IP)) {
				report = getHourlyReport(payload, VIEW);

				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(1000);
				problemStatistics.visitProblemReport(report);
				problemStatistics.setIps(new ArrayList<String>(report.getIps()));
				String response = m_gson.toJson(problemStatistics);
				model.setMobileResponse(response);
			} else {
				report = showHourlyReport(model, payload);

				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(1000);
				problemStatistics.visitProblemReport(report);
				ProblemStatistics statistics = model.getAllStatistics();
				statistics.setIps(new ArrayList<String>(report.getIps()));
				model.setMobileResponse(m_gson.toJson(statistics));
			}
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	public void normalize(Model model, Payload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		setDefaultThreshold(model, payload);

		String ip = payload.getIpAddress();
		if (StringUtils.isEmpty(ip)) {
			ip = CatString.ALL_IP;
		}
		model.setIpAddress(ip);
		model.setLongDate(payload.getDate());
		model.setAction(payload.getAction());
		model.setPage(ReportPage.PROBLEM);
		model.setDisplayDomain(payload.getDomain());
		model.setThreshold(payload.getLongTime());
		model.setSqlThreshold(payload.getSqlLongTime());
		if (payload.getPeriod().isCurrent()) {
			model.setCreatTime(new Date());
		} else {
			model.setCreatTime(new Date(payload.getDate() + 60 * 60 * 1000 - 1000));
		}
		if (payload.getAction() == Action.HISTORY) {
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

	private void setDefaultThreshold(Model model, Payload payload) {
		Map<String, Domain> domains = m_manager.getLongConfigDomains();
		Domain d = domains.get(payload.getDomain());

		if (d != null) {
			int longUrlTime = d.getUrlThreshold();
			if (payload.getRealLongTime() == 0) {
				payload.setLongTime(longUrlTime);
			}

			if (longUrlTime != 500 && longUrlTime != 1000 && longUrlTime != 2000 && longUrlTime != 3000
			      && longUrlTime != 4000 && longUrlTime != 5000) {
				double sec = (double) (longUrlTime) / (double) 1000;
				NumberFormat nf = new DecimalFormat("#.##");
				String option = "<option value=\"" + longUrlTime + "\"" + ">" + nf.format(sec) + " Sec</option>";

				model.setDefaultThreshold(option);
			}

			int longSqlTime = d.getSqlThreshold();
			if (payload.getSqlLongTime() == 0) {
				payload.setSqlLongTime(longSqlTime);
			}

			if (longSqlTime != 100 && longSqlTime != 500 && longSqlTime != 1000) {
				double sec = (double) (longSqlTime);
				NumberFormat nf = new DecimalFormat("#");
				String option = "<option value=\"" + longSqlTime + "\"" + ">" + nf.format(sec) + " ms</option>";

				model.setDefaultSqlThreshold(option);
			}
		}
	}

	private void showDetail(Model model, Payload payload) {
		String ipAddress = payload.getIpAddress();
		model.setLongDate(payload.getDate());
		model.setIpAddress(ipAddress);
		model.setGroupName(payload.getGroupName());
		model.setCurrentMinute(payload.getMinute());
		model.setThreadId(payload.getThreadId());

		ProblemReport report = getHourlyReport(payload, DETAIL);

		if (report == null) {
			return;
		}
		model.setReport(report);
		DetailStatistics detail = new DetailStatistics();
		detail.setIp(ipAddress).setMinute(payload.getMinute());
		detail.setGroupName(payload.getGroupName()).setThreadId(payload.getThreadId());
		detail.visitProblemReport(report);
		model.setDetailStatistics(detail);
	}

	private ProblemReport showHourlyReport(Model model, Payload payload) {
		ModelPeriod period = payload.getPeriod();
		if (period.isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		if (period.isCurrent() || period.isFuture()) {
			Calendar cal = Calendar.getInstance();
			int minute = cal.get(Calendar.MINUTE);

			model.setLastMinute(minute);
		} else {
			model.setLastMinute(59);
		}
		model.setHour(getHour(model.getLongDate()));
		ProblemReport report = getHourlyReport(payload, DETAIL);
		if (report != null) {
			String ip = getIpAddress(report, payload);

			model.setIpAddress(ip);
			model.setReport(report);
		}
		return report;
	}

	private ProblemReport showSummarizeReport(Model model, Payload payload) {
		String domain = model.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		ProblemReport problemReport = null;
		Date currentDayStart = TaskHelper.todayZero(new Date());

		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "problem",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findAllByDomainNameDuration(start, end, null, "problem",
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domains = new HashSet<String>();
				for (Report report : allReports) {
					domains.add(report.getDomain());
				}
				return m_problemMerger.mergeForDaily(domain, reports, domains);
			} catch (DalException e) {
				Cat.logError(e);
				return new ProblemReport(domain);
			}
		}
		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "problem",
			      DailyreportEntity.READSET_FULL);
			ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));
			for (Dailyreport report : reports) {
				String xml = report.getContent();
				ProblemReport reportModel = DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			}
			problemReport = merger.getProblemReport();
		} catch (Exception e) {
			Cat.logError(e);
		}
		return problemReport;
	}

	public enum DetailOrder {
		TYPE, STATUS, TOTAL_COUNT, DETAIL
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, DETAIL
	}
}
