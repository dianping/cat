package com.dianping.cat.report.page.failure;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	private static final String DEFAULT_DOMAIN = "User";

	private static final String MEMORY_CURRENT = "memory-current";

	private static final String MEMORY_LAST = "memory-last";

	private static final long MINUTE = 60 * 1000L;

	private static final long HOUR = 60 * MINUTE;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHH");

	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

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
		String file = getFailureReportName(payload, model);
		String domain = payload.getDomain();
		if (null == domain) {
			model.setDomain(DEFAULT_DOMAIN);
			domain = DEFAULT_DOMAIN;
		} else {
			model.setDomain(payload.getDomain());
		}
		if (file.equals(MEMORY_CURRENT)) {
			FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");

			if (analyzer == null) {
				System.out.println("analyzer is null");
				model.setReport(new FailureReport());
			} else {
				model.setReport(analyzer.generateByDomain(domain));
			}
		} else if (file.equals(MEMORY_LAST)) {
			FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getLastAnalyzer("failure");

			if (analyzer == null) {
				System.out.println("analyzer is null");
				model.setReport(new FailureReport());
			} else {
				//TODO
				getReport("");
				model.setReport(analyzer.generateByDomain(domain));
			}
		} else {
			model.setReport(new FailureReport());
		}
		m_jspViewer.view(ctx, model);
	}

	private String getFailureReportName(Payload payload, Model model) {
		long currentTime = System.currentTimeMillis();
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
		String domain = payload.getDomain();
		if (null == domain) {
			model.setDomain(DEFAULT_DOMAIN);
			domain = DEFAULT_DOMAIN;
		} else {
			model.setDomain(payload.getDomain());
		}

		long computeStart = startLong + payload.getMethod() * HOUR;
		if (computeStart > currentStart) {
			computeStart = currentStart;
		}
		model.setCurrent(SDF.format(new Date(computeStart)));

		if (computeStart == currentStart) {
			return MEMORY_CURRENT;
		} else if (computeStart == lastStart) {
			return MEMORY_LAST;
		}
		StringBuilder result = new StringBuilder();

		result.append(domain).append("@").append(SDF.format(new Date(computeStart))).append("~").append(
		      SDF.format(new Date(computeStart + HOUR)));
		return result.toString();
	}

	private FailureReport getReport(String file) {
		// TODO
		//Get to json data and output
		return new FailureReport();
	}
}
