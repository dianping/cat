package com.dianping.cat.report.page.failure;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	private static final String MEMORY_CURRENT = "memory-current";

	private static final String MEMORY_LAST = "memory-last";

	private static final long MINUTE = 60 * 1000L;

	private static final long HOUR = 60 * MINUTE;

	private static final long SECOND = 1000L;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");

	private static final SimpleDateFormat SDF_SEG = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static String DEFAULT_IP = null;

	private static String DEFAULT_DOMAIN = null;

	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	private String getFailureReportName(Payload payload, Model model) {
		long currentTimeMillis = System.currentTimeMillis();
		long currentTime = currentTimeMillis;
		long currentStart = currentTime - currentTime % HOUR;
		long lastStart = currentTime - currentTime % HOUR - HOUR;
		long startLong = currentStart;

		String reportStart = payload.getCurrent();
		if (reportStart != null) {
			try {
				Date reportStartDate = SDF.parse(reportStart);
				startLong = reportStartDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			reportStart = SDF.format(currentStart);
		}

		long computeStart = startLong + payload.getMethod() * HOUR;
		if (computeStart > currentStart) {
			computeStart = currentStart;
		}
		model.setCurrent(SDF.format(new Date(computeStart)));

		long titleEndTime = computeStart + HOUR - SECOND;
		if (titleEndTime > currentTimeMillis) {
			titleEndTime = currentTimeMillis;
		}
		StringBuilder title = new StringBuilder().append("Domain:").append(model.getCurrentDomain());
		title.append("  IP ").append(model.getCurrentIp());
		title.append("  From ").append(SDF_SEG.format(new Date(computeStart))).append(" To ").append(
		      SDF_SEG.format(new Date(titleEndTime)));
		model.setReportTitle(title.toString());

		if (computeStart == currentStart) {
			return MEMORY_CURRENT;
		} else if (computeStart == lastStart) {
			return MEMORY_LAST;
		}
		StringBuilder result = new StringBuilder();

		result.append(model.getCurrentDomain()).append(model.getCurrentIp()).append("-").append(SDF.format(new Date(computeStart))).append("-").append(
		      SDF.format(new Date(computeStart + HOUR - MINUTE))).append(".html");
		return result.toString();
	}

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
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.FAILURE);
		
		Payload payload = ctx.getPayload();
		
		FailureReportAnalyzer analyzerForPage = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		
		//Set all domain of page
		List<String> domains = analyzerForPage.getAllDomains();
		Collections.sort(domains);
		model.setDomains(domains);
		//Set all ip of the domain
		//Set the default domain and default ip
		String domain = payload.getDomain();
		if (null == domain) {
			if(domains!=null&&domains.size()>0){
				DEFAULT_DOMAIN = domains.get(0);
				model.setCurrentDomain(DEFAULT_DOMAIN);
			}else{
				throw new RuntimeException("The domain is null!");
			}
		} else {
			model.setCurrentDomain(domain);
		}
		
		List<String> ips = analyzerForPage.getHostIpByDomain(model.getCurrentDomain());
		Collections.sort(ips);
		model.setIps(ips);
		String ip = payload.getIp();
		if(null==ip){
			if(ips!=null&&ips.size()>0){
				DEFAULT_IP = ips.get(0);
				model.setCurrentIp(DEFAULT_IP);
			}else{
				throw new RuntimeException("The ip is null!");
			}
		} else {
			model.setCurrentIp(ip);
		}

		String file = getFailureReportName(payload, model);
		domain = model.getCurrentDomain();
		ip = model.getCurrentIp();
		String jsonResult="";
		if (file.equals(MEMORY_CURRENT) || file.equals(MEMORY_LAST)) {
			FailureReportAnalyzer analyzer;
			int pos = 0;
			if (file.equals(MEMORY_CURRENT)) {
				analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
				pos = 1;
			} else {
				analyzer = (FailureReportAnalyzer) m_consumer.getLastAnalyzer("failure");
				pos = 0;
			}
			if (analyzer == null) {
				jsonResult= FailureData.getFailureDataByNew(pos, domain, ip);
			} else {
				 jsonResult = FailureData.getFailureDataFromMemory(analyzer, domain, ip);
			}
			model.setJsonResult(jsonResult);
		} else {
			String baseFilePath = analyzerForPage.getReportPath();
			jsonResult =FailureData.getFailureDataFromFile(baseFilePath, file);
		}
		model.setJsonResult(jsonResult);
		m_jspViewer.view(ctx, model);
	}
}
