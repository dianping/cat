package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
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

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_service;

	@Inject
	private NormalizePayload m_normalizePayload;

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
		if (!CatString.ALL.equals(payload.getIpAddress())) {
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
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), "problem");
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
		int urlThreshold = payload.getLongTime();
		int sqlThreshold = payload.getSqlLongTime();
		int serviceThreshold = payload.getSeviceLongTime();

		switch (payload.getAction()) {
		case VIEW:
			report = getHourlyReport(payload, VIEW);
			model.setReport(report);
			if (ip.equals(CatString.ALL)) {
				problemStatistics.setAllIp(true);
			} else {
				problemStatistics.setIp(ip);
			}
			problemStatistics.setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold)
			      .setServiceThreshold(serviceThreshold);
			problemStatistics.visitProblemReport(report);
			model.setAllStatistics(problemStatistics);
			break;
		case HISTORY:
			report = showSummarizeReport(model, payload);
			if (ip.equals(CatString.ALL)) {
				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold)
				      .setServiceThreshold(serviceThreshold);
				problemStatistics.visitProblemReport(report);
			} else {
				problemStatistics.setIp(ip).setSqlThreshold(sqlThreshold).setUrlThreshold(urlThreshold)
				      .setServiceThreshold(serviceThreshold);
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
			if (ip.equals(CatString.ALL)) {
				report = getHourlyReport(payload, VIEW);

				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(1000)
				      .setServiceThreshold(serviceThreshold);
				problemStatistics.visitProblemReport(report);
				problemStatistics.setIps(new ArrayList<String>(report.getIps()));
				String response = m_gson.toJson(problemStatistics);
				model.setMobileResponse(response);
			} else {
				report = showHourlyReport(model, payload);

				problemStatistics.setAllIp(true).setSqlThreshold(sqlThreshold).setUrlThreshold(1000)
				      .setServiceThreshold(serviceThreshold);
				problemStatistics.visitProblemReport(report);
				ProblemStatistics statistics = model.getAllStatistics();
				statistics.setIps(new ArrayList<String>(report.getIps()));
				model.setMobileResponse(m_gson.toJson(statistics));
			}
			break;
		case HOUR_GRAPH:
			report = getHourlyReport(payload, DETAIL);
			String type = payload.getType();
			String state = payload.getStatus();
			Date start = report.getStartTime();
			ProblemReportVisitor vistor = new ProblemReportVisitor(ip, type, state, start);

			vistor.visitProblemReport(report);
			model.setErrorsTrend(m_gson.toJson(vistor.getGraphItem()));
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		setDefaultThreshold(model, payload);
		model.setPage(ReportPage.PROBLEM);
		model.setThreshold(payload.getLongTime());
		model.setSqlThreshold(payload.getSqlLongTime());
		model.setServiceThreshold(payload.getSeviceLongTime());

		m_normalizePayload.normalize(model, payload);
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
