package com.dianping.cat.report.page.transaction;

import java.io.IOException;
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
import com.dianping.cat.report.tool.Constants;
import com.dianping.cat.report.tool.DateUtils;
import com.dianping.cat.report.tool.ReportUtils;
import com.dianping.cat.report.tool.StringUtils;
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

	@Inject
	private TransactionManage m_manager;

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
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TRANSACTION);
		
		long currentTimeMillis = System.currentTimeMillis();
		long currentTime = currentTimeMillis - currentTimeMillis % DateUtils.HOUR;
		int method = payload.getMethod();
		String currentDomain = payload.getDomain();
		String current = payload.getCurrent();
		long reportStart = m_manager.computeReportStartHour(currentTime, current, method);
		String reportCurrentTime = DateUtils.SDF_URL.format(new Date(reportStart));
		String index = m_manager.getReportStartType(currentTime, reportStart);
		
		model.setCurrent(reportCurrentTime);
		model.setCurrent(DateUtils.SDF_URL.format(new Date(reportStart)));
		if (index.equals(Constants.MEMORY_CURRENT) || index.equals(Constants.MEMORY_LAST)) {
			List<TransactionReport> reports = new ArrayList<TransactionReport>();
			List<String> servers = serverConfig.getConsumerServers();
			Set<String> domains = new HashSet<String>();
			for (String server : servers) {
				String connectionUrl = m_manager.getConnectionUrl(server, currentDomain,index);
				String pageResult = m_manager.getRemotePageContent(connectionUrl);
				if(pageResult!=null){
					List<String> domainTemps = StringUtils.getListFromPage(pageResult, "<domains>", "</domains>");
					if (domainTemps != null) {
						for (String temp : domainTemps) {
							domains.add(temp);
						}
					}
					String xml = StringUtils.getStringFromPage(pageResult, "<data>", "</data>");
					reports.add(ReportUtils.parseTransactionReportXML(xml));
				}else{
					reports.add(new TransactionReport(currentDomain));
				}
			}
			TransactionReport result = ReportUtils.mergeTransactionReports(reports);
			List<String> domainList = new ArrayList<String>(domains);

			Collections.sort(domainList);
			model.setDomains(domainList);
			currentDomain = result.getDomain();
			model.setCurrentDomain(currentDomain);
			model.setJsonResult(new DefaultJsonBuilder().buildJson(result));
			model.setGenerateTime(DateUtils.SDF_SEG.format(new Date()));
		} else {
			// TODO
			model.setGenerateTime(DateUtils.SDF_SEG.format(new Date(reportStart+DateUtils.HOUR)));
			String reportFileName = m_manager.getReportStoreFile(reportStart, payload.getDomain());
			model.setCurrentDomain(payload.getDomain());
			System.out.println(reportFileName);
		}
		model.setType(payload.getType());
		model.setUrlPrefix(m_manager.getBaseUrl(currentDomain, reportCurrentTime));
		model.setReportTitle(m_manager.getReportDisplayTitle(model.getCurrentDomain(), reportStart));
		m_jspViewer.view(ctx, model);
	}
}
