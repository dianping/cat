package com.dianping.cat.report.page.cross;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private NormalizePayload m_normalizePayload;
	
	@Inject
	private DomainManager m_domainManager;

	@Inject(type = ModelService.class, value = "cross")
	private ModelService<CrossReport> m_service;

	private ProjectInfo buildCallProjectInfo(String domain, ModelPeriod period, String date, long duration) {
		CrossReport projectReport = getHourlyReport(domain, period, date, CatString.ALL);
		ProjectInfo projectInfo = new ProjectInfo(duration);

		projectInfo.setDomainManager(m_domainManager);
		projectInfo.setClientIp(CatString.ALL);
		projectInfo.visitCrossReport(projectReport);

		return projectInfo;
	}

	private ProjectInfo buildHistoryCallProjectInfo(String domain, Date start, Date end) {
		CrossReport projectReport = getSummarizeReport(domain, start, end);
		ProjectInfo projectInfo = new ProjectInfo(end.getTime() - start.getTime());

		projectInfo.setDomainManager(m_domainManager);
		projectInfo.setClientIp(CatString.ALL);
		projectInfo.visitCrossReport(projectReport);
		return projectInfo;
	}

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
			
			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), "cross");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable cross service registered for " + request + "!");
		}
	}

	private CrossReport getHourlyReport(String domain, ModelPeriod period, String date, String ip) {
		ModelRequest request = new ModelRequest(domain, period) //
		      .setProperty("date", date) //
		      .setProperty("ip", ip);

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

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryCrossReport(domain, start, end);
	}

	private CrossReport getSummarizeReport(String domain, Date start, Date end) {
		return m_reportService.queryCrossReport(domain, start, end);
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
		long historyTime = payload.getHistoryEndDate().getTime() - payload.getHistoryStartDate().getTime();

		String domain = payload.getDomain();
		switch (payload.getAction()) {
		case HOURLY_PROJECT:
			CrossReport projectReport = getHourlyReport(payload);
			ProjectInfo projectInfo = new ProjectInfo(payload.getHourDuration());

			projectInfo.setDomainManager(m_domainManager);
			projectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			projectInfo.visitCrossReport(projectReport);
			model.setProjectInfo(projectInfo);
			model.setReport(projectReport);

			if (payload.getIpAddress().equals(CatString.ALL)) {
				List<TypeDetailInfo> details = projectInfo.getServiceProjectsInfo();

				for (TypeDetailInfo info : details) {
					String projectName = info.getProjectName();
					if (projectName.equalsIgnoreCase(payload.getDomain()) || projectName.equalsIgnoreCase("UnknownProject")
					      || projectName.equalsIgnoreCase(ProjectInfo.ALL_CLIENT)) {
						continue;
					}
					ProjectInfo temp = buildCallProjectInfo(projectName, payload.getPeriod(),
					      String.valueOf(payload.getDate()), payload.getHourDuration());

					TypeDetailInfo detail = temp.getAllCallProjectInfo().get(domain);

					if (detail != null) {
						detail.setProjectName(projectName);
						projectInfo.getAllCallServiceProjectsInfo().put(projectName, detail);
					}
				}
			}
			break;
		case HOURLY_HOST:
			CrossReport hostReport = getHourlyReport(payload);
			HostInfo hostInfo = new HostInfo(payload.getHourDuration());

			hostInfo.setDomainManager(m_domainManager);
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

			methodInfo.setDomainManager(m_domainManager);
			methodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort()).setRemoteProject(payload.getProjectName());
			methodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			methodInfo.visitCrossReport(methodReport);
			model.setReport(methodReport);
			model.setMethodInfo(methodInfo);
			break;
		case HISTORY_PROJECT:
			CrossReport historyProjectReport = getSummarizeReport(payload);
			ProjectInfo historyProjectInfo = new ProjectInfo(historyTime);

			historyProjectInfo.setDomainManager(m_domainManager);
			historyProjectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			historyProjectInfo.visitCrossReport(historyProjectReport);
			model.setProjectInfo(historyProjectInfo);
			model.setReport(historyProjectReport);

			if (payload.getIpAddress().equals(CatString.ALL)) {
				List<TypeDetailInfo> details = historyProjectInfo.getServiceProjectsInfo();

				for (TypeDetailInfo info : details) {
					String projectName = info.getProjectName();
					if (projectName.equalsIgnoreCase(payload.getDomain()) || projectName.equalsIgnoreCase("UnknownProject")
					      || projectName.equalsIgnoreCase(ProjectInfo.ALL_CLIENT)) {
						continue;
					}
					Date start = payload.getHistoryStartDate();
					Date end = payload.getHistoryEndDate();
					ProjectInfo temp = buildHistoryCallProjectInfo(projectName, start, end);

					TypeDetailInfo detail = temp.getAllCallProjectInfo().get(domain);

					if (detail != null) {
						detail.setProjectName(projectName);
						historyProjectInfo.getAllCallServiceProjectsInfo().put(projectName, detail);
					}
				}
			}
			break;
		case HISTORY_HOST:
			CrossReport historyHostReport = getSummarizeReport(payload);
			HostInfo historyHostInfo = new HostInfo(historyTime);

			historyHostInfo.setDomainManager(m_domainManager);
			historyHostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort());
			historyHostInfo.setProjectName(payload.getProjectName());
			historyHostInfo.visitCrossReport(historyHostReport);
			model.setReport(historyHostReport);
			model.setHostInfo(historyHostInfo);
			break;
		case HISTORY_METHOD:
			CrossReport historyMethodReport = getSummarizeReport(payload);
			MethodInfo historyMethodInfo = new MethodInfo(historyTime);

			historyMethodInfo.setDomainManager(m_domainManager);
			historyMethodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort())
			      .setServiceSortBy(model.getServiceSort()).setRemoteProject(payload.getProjectName());
			historyMethodInfo.setRemoteIp(payload.getRemoteIp()).setQuery(model.getQueryName());
			historyMethodInfo.visitCrossReport(historyMethodReport);
			model.setReport(historyMethodReport);
			model.setMethodInfo(historyMethodInfo);
			break;

		case METHOD_QUERY:
			String method = payload.getMethod();
			CrossMethodVisitor info = new CrossMethodVisitor(method, m_domainManager);
			String reportType = payload.getReportType();
			CrossReport queryReport = null;

			if (reportType != null
			      && (reportType.equals("day") || reportType.equals("week") || reportType.equals("month"))) {
				queryReport = getSummarizeReport(payload);
			} else {
				queryReport = getHourlyReport(payload);
			}
			info.visitCrossReport(queryReport);
			model.setReport(queryReport);
			model.setInfo(info.getInfo());
			break;
		}
		m_jspViewer.view(ctx, model);
	}
	
	private void normalize(Model model,Payload payload){
		model.setPage(ReportPage.CROSS);
		m_normalizePayload.normalize(model, payload);
		
		if (StringUtils.isEmpty(payload.getCallSort())) {
			payload.setCallSort("avg");
		}
		if (StringUtils.isEmpty(payload.getServiceSort())) {
			payload.setServiceSort("avg");
		}
		model.setCallSort(payload.getCallSort());
		model.setServiceSort(payload.getServiceSort());
		model.setQueryName(payload.getQueryName());
		
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
	}

}
