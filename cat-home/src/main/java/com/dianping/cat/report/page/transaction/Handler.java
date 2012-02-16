package com.dianping.cat.report.page.transaction;

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

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.ServerConfig;
import com.dianping.cat.report.tool.BaseReportTool;
import com.dianping.cat.report.tool.Constant;
import com.dianping.cat.report.tool.DateUtil;
import com.dianping.cat.report.tool.ServicePageTool;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

/**
 * @author sean.wang
 * @since Feb 6, 2012
 */
public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfig serverConfig;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		long currentTimeMillis = System.currentTimeMillis();
		long currentTime = currentTimeMillis - currentTimeMillis % DateUtil.HOUR;

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TRANSACTION);

		String domain = payload.getDomain();
		String current = payload.getCurrent();
		int method = payload.getMethod();
		long reportStart = BaseReportTool.computeReportStart(currentTime, current, method);

		model.setCurrent(DateUtil.SDF_URL.format(new Date(reportStart)));
		String index = BaseReportTool.getReportIndex(currentTime, reportStart);

		if (index.equals(Constant.MEMORY_CURRENT) || index.equals(Constant.MEMORY_LAST)) {
			List<TransactionReport> reports = new ArrayList<TransactionReport>();
			List<String> servers = serverConfig.getConsumerServers();
			Set<String> domains = new HashSet<String>();

			for (String server : servers) {
				URL url = new URL(BaseReportTool.getConnectionUrl("transaction", server, domain, "", index));
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
					String xml = ServicePageTool.getStringFromPage(pageResult, "<data>", "</data>");
					reports.add(TransactionReportTool.parseXML(xml));
				} else {
					// TODO the remote server have some problem
				}
			}
			TransactionReport result = TransactionReportTool.merge(reports);
			List<String> domainList = new ArrayList<String>(domains);
			
			Collections.sort(domainList);
			model.setDomains(domainList);
			model.setCurrentDomain(result.getDomain());
			model.setJsonResult(new DefaultJsonBuilder().buildJson(result));

		} else {
			// TODO
			String reportFileName = BaseReportTool.getReportName(reportStart, payload.getDomain(), "");
			model.setCurrentDomain(payload.getDomain());
			System.out.println(reportFileName);
		}
		String title = BaseReportTool.getReportTitle("transaction", model.getCurrentDomain(), "", reportStart);
		model.setReportTitle(title);
		m_jspViewer.view(ctx, model);
	}
}
