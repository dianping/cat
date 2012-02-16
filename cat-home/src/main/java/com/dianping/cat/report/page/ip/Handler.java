package com.dianping.cat.report.page.ip;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Period;
import com.dianping.cat.consumer.ip.model.transform.BaseVisitor;
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

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private ServerConfig serverConfig;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "ip")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "ip")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		IpReport report = null ;
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.IP);
		
		long currentTimeMillis = System.currentTimeMillis();
		long currentTime = currentTimeMillis - currentTimeMillis % DateUtil.HOUR;

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.FAILURE);

		String domain = payload.getDomain();

		String urlCurrent = payload.getCurrent();
		int method = payload.getMethod();
		long reportStart = BaseReportTool.computeReportStart(currentTime,urlCurrent, method);
		model.setCurrent(DateUtil.SDF_URL.format(new Date(reportStart)));
		String index = BaseReportTool.getReportIndex(currentTime,reportStart);

		if (index.equals(Constant.MEMORY_CURRENT) || index.equals(Constant.MEMORY_LAST)) {
			List<IpReport> reports = new ArrayList<IpReport>();
			List<String> servers = serverConfig.getConsumerServers();
			Set<String> domains = new HashSet<String>();

			for (String server : servers) {
				URL url = new URL(BaseReportTool.getConnectionUrl("ip", server, domain, "", index));
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
					reports.add(IpReportTool.parseXML(xml));
				} else {
					// TODO the remote server have some problem
				}
			}
			report = IpReportTool.merge(reports);
			List<String> domainList = new ArrayList<String>(domains);
			Collections.sort(domainList);
			model.setDomains(domainList);
			model.setCurrentDomain(report.getDomain());

		} else {
			// TODO
			String reportFileName = BaseReportTool.getReportName(reportStart, payload.getDomain(), "");
			System.out.println(reportFileName);
		}
		String title = BaseReportTool.getReportTitle("failure", model.getCurrentDomain(), "", reportStart);
		model.setReportTitle(title);

		//TODO get the all report 
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		Map<String, DisplayModel> models = new HashMap<String, DisplayModel>();
		DisplayModelBuilder builder = new DisplayModelBuilder(models, minute);

		report.accept(builder); // prepare display model

		List<DisplayModel> displayModels = new ArrayList<DisplayModel>(models.values());

		Collections.sort(displayModels, new Comparator<DisplayModel>() {
			@Override
			public int compare(DisplayModel m1, DisplayModel m2) {
				return m2.getLastFifteen() - m1.getLastFifteen(); // desc
			}
		});

		model.setReport(report);
		model.setDisplayModels(displayModels);
		m_jspViewer.view(ctx, model);
	}

	static class DisplayModelBuilder extends BaseVisitor {
		private int m_minute;

		private Map<String, DisplayModel> m_models;

		private Period m_period;

		public DisplayModelBuilder(Map<String, DisplayModel> models, int minute) {
			m_models = models;
			m_minute = minute;
		}

		@Override
		public void visitIp(Ip ip) {
			String address = ip.getAddress();
			DisplayModel model = m_models.get(address);

			if (model == null) {
				model = new DisplayModel(address);
				m_models.put(address, model);
			}

			model.process(m_minute, m_period.getMinute(), ip.getCount());
		}

		@Override
		public void visitPeriod(Period period) {
			m_period = period;
			super.visitPeriod(period);
		}
	}
}
