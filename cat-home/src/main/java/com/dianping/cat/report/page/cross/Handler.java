package com.dianping.cat.report.page.cross;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.HostinfoDao;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.cross.CrossMerger;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private CrossMerger m_crossMerger;

	@Inject
	private HostinfoDao m_hostinfoDao;

	@Inject(type = ModelService.class, value = "cross")
	private ModelService<CrossReport> m_service;

	private CrossReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<CrossReport> response = m_service.invoke(request);
			CrossReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable cross service registered for " + request + "!");
		}
	}

	private CrossReport getSummarizeReport(Payload payload) {
		String domain = payload.getDomain();

		CrossReport crossReport = null;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "cross",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findAllByDomainNameDuration(start, end, null, null,
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domains = new HashSet<String>();
				for (Report report : allReports) {
					domains.add(report.getDomain());
				}
				crossReport = m_crossMerger.mergeForDaily(domain, reports, domains);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} else {
			try {
				List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "cross",
				      DailyreportEntity.READSET_FULL);
				CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
				for (Dailyreport report : reports) {
					String xml = report.getContent();
					CrossReport reportModel = DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				crossReport = merger.getCrossReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		if (crossReport == null) {
			return null;
		}
		crossReport.setStartTime(start);
		crossReport.setEndTime(end);

		return crossReport;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cross")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cross")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		switch (payload.getAction()) {
		case HOURLY_PROJECT:
			CrossReport projectReport = getHourlyReport(payload);
			ProjectInfo projectInfo = new ProjectInfo(payload.getHourDuration());

			projectInfo.setHostInfoDao(m_hostinfoDao);
			projectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			projectInfo.visitCrossReport(projectReport);
			model.setProjectInfo(projectInfo);
			model.setReport(projectReport);
			break;
		case HOURLY_HOST:
			CrossReport hostReport = getHourlyReport(payload);
			HostInfo hostInfo = new HostInfo(payload.getHourDuration());

			hostInfo.setHostInfoDao(m_hostinfoDao);
			hostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			hostInfo.setProjectName(payload.getProjectName());
			hostInfo.visitCrossReport(hostReport);
			model.setReport(hostReport);
			model.setHostInfo(hostInfo);
			break;
		case HOURLY_METHOD:
			CrossReport methodReport = getHourlyReport(payload);
			MethodInfo methodInfo = new MethodInfo(payload.getHourDuration());

			methodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			methodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			methodInfo.visitCrossReport(methodReport);
			model.setReport(methodReport);
			model.setMethodInfo(methodInfo);
			break;
		case HISTORY_PROJECT:
			CrossReport historyProjectReport = getSummarizeReport(payload);
			ProjectInfo historyProjectInfo = new ProjectInfo(payload.getHourDuration());

			historyProjectInfo.setHostInfoDao(m_hostinfoDao);
			historyProjectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			historyProjectInfo.visitCrossReport(historyProjectReport);
			model.setProjectInfo(historyProjectInfo);
			model.setReport(historyProjectReport);
			break;
		case HISTORY_HOST:
			CrossReport historyHostReport = getSummarizeReport(payload);
			HostInfo historyHostInfo = new HostInfo(payload.getHourDuration());

			historyHostInfo.setHostInfoDao(m_hostinfoDao);
			historyHostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			historyHostInfo.setProjectName(payload.getProjectName());
			historyHostInfo.visitCrossReport(historyHostReport);
			model.setReport(historyHostReport);
			model.setHostInfo(historyHostInfo);
			break;
		case HISTORY_METHOD:
			CrossReport historyMethodReport = getSummarizeReport(payload);
			MethodInfo historyMethodInfo = new MethodInfo(payload.getHourDuration());

			historyMethodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			historyMethodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			historyMethodInfo.visitCrossReport(historyMethodReport);
			model.setReport(historyMethodReport);
			model.setMethodInfo(historyMethodInfo);
			break;
		default:
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	public void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.CROSS);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		if (StringUtils.isEmpty(payload.getIpAddress())) {
			payload.setIpAddress("All");
		}
		if (StringUtils.isEmpty(payload.getCallSort())) {
			payload.setCallSort("avg");
		}
		if (StringUtils.isEmpty(payload.getServiceSort())) {
			payload.setServiceSort("avg");
		}
		model.setCallSort(payload.getCallSort());
		model.setServiceSort(payload.getServiceSort());
		model.setIpAddress(payload.getIpAddress());
		model.setDisplayDomain(payload.getDomain());
		model.setQueryName(payload.getQueryName());

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		if (StringUtils.isEmpty(payload.getProjectName())) {
			if (payload.getAction() == Action.HOURLY_HOST) {
				payload.setAction("view");
			}
			if (payload.getAction() == Action.HISTORY_HOST) {
				payload.setAction("history");
			}
		}

		if (StringUtils.isEmpty(payload.getRemoteIp())) {
			if (payload.getAction() == Action.HOURLY_METHOD) {
				payload.setAction("view");
			}
			if (payload.getAction() == Action.HISTORY_METHOD) {
				payload.setAction("history");
			}
		}
		action = payload.getAction();
		model.setAction(action);

		if (action == Action.HISTORY_PROJECT || action == Action.HISTORY_METHOD||action==Action.HISTORY_HOST) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());
			model.setCustomDate(payload.getHistoryStartDate(), payload.getHistoryEndDate());
		}
	}

}
