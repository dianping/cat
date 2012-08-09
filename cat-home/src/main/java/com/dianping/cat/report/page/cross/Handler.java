package com.dianping.cat.report.page.cross;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
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
			
			projectInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort()).setServiceSortBy(model.getServiceSort());
			projectInfo.visitCrossReport(projectReport);
			model.setProjectInfo(projectInfo);
			model.setReport(projectReport);
			break;
		case HOURLY_HOST:
			CrossReport hostReport = getHourlyReport(payload);
			HostInfo hostInfo = new HostInfo(payload.getHourDuration());
			
			hostInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort()).setServiceSortBy(model.getServiceSort());
			hostInfo.setProjectName(payload.getProjectName());
			hostInfo.visitCrossReport(hostReport);
			model.setReport(hostReport);
			model.setHostInfo(hostInfo);
			break;
		case HOURLY_METHOD:
			CrossReport methodReport = getHourlyReport(payload);
         MethodInfo methodInfo = new MethodInfo(payload.getHourDuration());
         
         methodInfo.setClientIp(model.getIpAddress()).setCallSortBy(model.getCallSort()).setServiceSortBy(model.getServiceSort());
         methodInfo.setRemoteIp(payload.getRemoteIp());
         methodInfo.visitCrossReport(methodReport);
         model.setReport(methodReport);
         model.setMethodInfo(methodInfo);
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
		if(StringUtils.isEmpty(payload.getCallSort())){
			payload.setCallSort("avg");
		}
		if(StringUtils.isEmpty(payload.getServiceSort())){
			payload.setServiceSort("avg");
		}
		model.setCallSort(payload.getCallSort());
		model.setServiceSort(payload.getServiceSort());
		model.setIpAddress(payload.getIpAddress());
		model.setDisplayDomain(payload.getDomain());

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
	}
}
