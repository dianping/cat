package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportAggregation;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.DomainGroupConfigManager;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {

	private static final String DETAIL = "detail";

	private static final String VIEW = "view";

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ReportService m_reportService;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	@Inject
	private DomainGroupConfigManager m_configManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProblemReportAggregation m_problemReportAggregation;

	private Gson m_gson = new Gson();

	private int getHour(long date) {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	private ProblemReport getHourlyReport(Payload payload, String type) {
		ProblemReport report = getHourlyReportInternal(payload, type);
		if (Constants.FRONT_END.equals(payload.getDomain())) {
			report = buildFrontEndByRule(report);
		}
		return report;
	}

	private ProblemReport buildFrontEndByRule(ProblemReport report) {
		report.accept(m_problemReportAggregation);
		return m_problemReportAggregation.getReport();
	}

	private ProblemReport getHourlyReportInternal(Payload payload, String type) {
		String domain = payload.getDomain();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("type", type);
		if (!Constants.ALL.equals(payload.getIpAddress())) {
			request.setProperty("ip", payload.getIpAddress());
		}
		if (!StringUtils.isEmpty(payload.getThreadId())) {
			request.setProperty("thread", payload.getThreadId());
		}
		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();
			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), ProblemAnalyzer.ID);
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}

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
		LongConfig longConfig = new LongConfig();
		Action action = payload.getAction();
		String domain = payload.getDomain();
		String group = payload.getGroup();

		longConfig.setSqlThreshold(payload.getSqlThreshold()).setUrlThreshold(payload.getUrlThreshold())
		      .setServiceThreshold(payload.getServiceThreshold());
		longConfig.setCacheThreshold(payload.getCacheThreshold()).setCallThreshold(payload.getCallThreshold());
		problemStatistics.setLongConfig(longConfig);
		model.setGroups(m_configManager.queryDomainGroup(payload.getDomain()));
		switch (action) {
		case HOULY_REPORT:
			report = getHourlyReport(payload, VIEW);
			model.setReport(report);
			if (ip.equals(Constants.ALL)) {
				problemStatistics.setAllIp(true);
			} else {
				problemStatistics.setIp(ip);
			}
			problemStatistics.visitProblemReport(report);
			model.setAllStatistics(problemStatistics);
			break;
		case HISTORY_REPORT:
			report = showSummarizeReport(model, payload);
			if (ip.equals(Constants.ALL)) {
				problemStatistics.setAllIp(true);
				problemStatistics.visitProblemReport(report);
			} else {
				problemStatistics.setIp(ip);
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
		case HOUR_GRAPH:
			report = getHourlyReport(payload, DETAIL);
			String type = payload.getType();
			String state = payload.getStatus();
			Date start = report.getStartTime();
			HourlyLineChartVisitor vistor = new HourlyLineChartVisitor(ip, type, state, start);

			vistor.visitProblemReport(report);
			model.setErrorsTrend(m_gson.toJson(vistor.getGraphItem()));
			break;
		case HOURLY_GROUP_REPORT:
			report = getHourlyReport(payload, VIEW);
			report = filterReportByGroup(report, domain, group);
			model.setReport(report);
			if (ip.equals(Constants.ALL)) {
				problemStatistics.setAllIp(true);
			} else {
				problemStatistics.setIp(ip);
			}
			problemStatistics.visitProblemReport(report);
			model.setAllStatistics(problemStatistics);
			break;
		case GROUP_GRAPHS:
			report = getHourlyReport(payload, DETAIL);
			report = filterReportByGroup(report, domain, group);
			type = payload.getType();
			state = payload.getStatus();
			start = report.getStartTime();
			vistor = new HourlyLineChartVisitor(ip, type, state, start);
			vistor.visitProblemReport(report);
			model.setErrorsTrend(m_gson.toJson(vistor.getGraphItem()));
			break;
		case  HISTORY_GROUP_REPORT:
			report = showSummarizeReport(model, payload);
			report = filterReportByGroup(report, domain, group);
			if (ip.equals(Constants.ALL)) {
				problemStatistics.setAllIp(true);
				problemStatistics.visitProblemReport(report);
			} else {
				problemStatistics.setIp(ip);
				problemStatistics.visitProblemReport(report);
			}
			model.setReport(report);
			model.setAllStatistics(problemStatistics);
		case HISTORY_GROUP_GRAPH:
			m_historyGraphs.buildTrendGraph(model, payload);
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
		}
		m_jspViewer.view(ctx, model);
	}

	private ProblemReport filterReportByGroup(ProblemReport report, String domain, String group) {
		List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);
		List<String> removes = new ArrayList<String>();

		for (Machine machine : report.getMachines().values()) {
			String ip = machine.getIp();

			if (!ips.contains(ip)) {
				removes.add(ip);
			}
		}
		for (String ip : removes) {
			report.getMachines().remove(ip);
		}
		return report;
	}

	private void normalize(Model model, Payload payload) {
		setDefaultThreshold(model, payload);
		model.setPage(ReportPage.PROBLEM);
		m_normalizePayload.normalize(model, payload);
	}

	private void setDefaultThreshold(Model model, Payload payload) {
		Map<String, Domain> domains = m_manager.getLongConfigDomains();
		Domain d = domains.get(payload.getDomain());

		if (d != null) {
			int longUrlTime = d.getUrlThreshold() == null ? m_manager.getLongUrlDefaultThreshold() : d.getUrlThreshold()
			      .intValue();

			if (longUrlTime != 500 && longUrlTime != 1000 && longUrlTime != 2000 && longUrlTime != 3000
			      && longUrlTime != 4000 && longUrlTime != 5000) {
				double sec = (double) (longUrlTime) / (double) 1000;
				NumberFormat nf = new DecimalFormat("#.##");
				String option = "<option value=\"" + longUrlTime + "\"" + ">" + nf.format(sec) + " Sec</option>";

				model.setDefaultThreshold(option);
			}

			int longSqlTime = d.getSqlThreshold();

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
		ProblemReport problemReport = m_reportService.queryProblemReport(domain, start, end);

		return problemReport;
	}

	public enum DetailOrder {
		TYPE, STATUS, TOTAL_COUNT, DETAIL
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, DETAIL
	}
}
