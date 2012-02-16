package com.dianping.cat.report.page.failure;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
import com.dianping.cat.tool.BaseReportTool;
import com.dianping.cat.tool.Constant;
import com.dianping.cat.tool.DateUtil;
import com.dianping.cat.tool.ServicePageTool;
import com.site.helper.Files;
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
		long currentTimeMillis = System.currentTimeMillis();
		long currentTime = currentTimeMillis - currentTimeMillis % DateUtil.HOUR;

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.FAILURE);

		String domain = payload.getDomain();
		String ip = payload.getIp();

		String urlCurrent = payload.getCurrent();
		int method = payload.getMethod();
		long reportStart = BaseReportTool.computeReportStart(currentTime,urlCurrent, method);
		model.setCurrent(DateUtil.SDF_URL.format(new Date(reportStart)));
		String index = BaseReportTool.getReportIndex(currentTime,reportStart);

		if (index.equals(Constant.MEMORY_CURRENT) || index.equals(Constant.MEMORY_LAST)) {
			List<FailureReport> reports = new ArrayList<FailureReport>();
			List<String> servers = serverConfig.getConsumerServers();
			Set<String> domains = new HashSet<String>();
			Set<String> ips = new HashSet<String>();

			for (String server : servers) {
				URL url = new URL(BaseReportTool.getConnectionUrl("failure", server, domain, ip, index));
				URLConnection URLconnection = url.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
				int responseCode = httpConnection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream input = httpConnection.getInputStream();
					String pageResult = Files.forIO().readFrom(input, "utf-8");
					List<String> domainTemps = ServicePageTool.getListFromPage(pageResult, "<domains>", "</domains>");
					if (domainTemps != null) {
						for (String temp : domainTemps) {
							domains.add(temp);
						}
					}
					List<String> ipsTemps = ServicePageTool.getListFromPage(pageResult, "<ips>", "</ips>");
					if (domainTemps != null) {
						for (String temp : ipsTemps) {
							ips.add(temp);
						}
					}
					String xml = ServicePageTool.getStringFromPage(pageResult, "<data>", "</data>");
					reports.add(FailureReportTool.parseXML(xml));
				} else {
					// TODO the remote server have some problem
				}
			}
			FailureReport result = FailureReportTool.merge(reports);
			List<String> domainList = new ArrayList<String>(domains);
			List<String> ipList = new ArrayList<String>(ips);
			
			Collections.sort(domainList);
			Collections.sort(ipList);
			model.setDomains(domainList);
			model.setIps(ipList);
			model.setCurrentDomain(result.getDomain());
			model.setCurrentIp(result.getMachine());
			model.setJsonResult(new DefaultJsonBuilder().buildJson(result));
		} else {
			// TODO
			String reportFileName = BaseReportTool.getReportName(reportStart, payload.getDomain(), payload.getIp());
			model.setCurrentDomain(payload.getDomain());
			model.setCurrentIp(payload.getIp());
			System.out.println(reportFileName);
		}
		String title = BaseReportTool.getReportTitle("failure", model.getCurrentDomain(), model.getCurrentIp(), reportStart);
		model.setReportTitle(title);
		m_jspViewer.view(ctx, model);
	}
}
