package com.dianping.cat.report.page.failure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.ServerConfig;
import com.dianping.cat.report.tool.Constants;
import com.dianping.cat.report.tool.DateUtils;
import com.dianping.cat.report.tool.ReportUtils;
import com.dianping.cat.report.tool.StringUtils;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private ServerConfig serverConfig;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private FailureManager m_manager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "f")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "f")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.FAILURE);

		long currentTimeMillis = System.currentTimeMillis();
		long currentHour = currentTimeMillis - currentTimeMillis % DateUtils.HOUR;
		String currentDomain = payload.getDomain();
		String currentIp = payload.getIp();
		String urlCurrent = payload.getCurrent();
		int method = payload.getMethod();
		long reportStart = m_manager.computeReportStartHour(currentHour, urlCurrent, method);
		String reportCurrentTime = DateUtils.SDF_URL.format(new Date(reportStart));
		String index = m_manager.getReportStartType(currentHour, reportStart);

		model.setCurrent(reportCurrentTime);
		if (index.equals(Constants.MEMORY_CURRENT) || index.equals(Constants.MEMORY_LAST)) {
			List<FailureReport> reports = new ArrayList<FailureReport>();
			List<String> servers = serverConfig.getConsumerServers();
			Set<String> domains = new HashSet<String>();
			Set<String> ips = new HashSet<String>();

			for (String server : servers) {
				String connectionUrl = m_manager.getConnectionUrl(server, currentDomain, currentIp, index);
				String pageResult = m_manager.getRemotePageContent(connectionUrl);
				if(pageResult!=null){
					List<String> domainTemps = StringUtils.getListFromPage(pageResult, "<domains>", "</domains>");
					if (domainTemps != null) {
						for (String temp : domainTemps) {
							domains.add(temp);
						}
					}
					List<String> ipsTemps = StringUtils.getListFromPage(pageResult, "<ips>", "</ips>");
					if (domainTemps != null) {
						for (String temp : ipsTemps) {
							ips.add(temp);
						}
					}
					String xml = StringUtils.getStringFromPage(pageResult, "<data>", "</data>");
					reports.add(ReportUtils.parseFailureReportXML(xml));
				}else{
					reports.add(new FailureReport());
				}
			}
			FailureReport result = ReportUtils.mergeFailureReports(reports);
			List<String> domainList = new ArrayList<String>(domains);
			List<String> ipList = new ArrayList<String>(ips);

			Collections.sort(domainList);
			Collections.sort(ipList);
			model.setDomains(domainList);
			model.setIps(ipList);
			currentDomain = result.getDomain();
			model.setCurrentDomain(currentDomain);
			currentIp = result.getMachine();
			model.setCurrentIp(currentIp);
			model.setJsonResult(new DefaultJsonBuilder().buildJson(result));
			model.setGenerateTime(DateUtils.SDF_SEG.format(new Date()));
		} else {
			// TODO
			model.setGenerateTime(DateUtils.SDF_SEG.format(new Date(reportStart + DateUtils.HOUR)));
			String reportFileName = m_manager.getReportStoreFile(reportStart, payload.getDomain(), payload.getIp());
			model.setCurrentDomain(payload.getDomain());
			model.setCurrentIp(payload.getIp());
			System.out.println(reportFileName);
		}
		model.setUrlPrefix(m_manager.getBaseUrl(currentDomain, currentIp, reportCurrentTime));
		model.setReportTitle(m_manager.getReportDisplayTitle(model.getCurrentDomain(), model.getCurrentIp(), reportStart));
		m_jspViewer.view(ctx, model);
	}
}
