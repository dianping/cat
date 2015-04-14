package com.dianping.cat.report.page.top;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.page.dependency.ExternalInfoBuilder;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ExternalInfoBuilder m_externalInfoBuilder;

	@Inject
	private StateBuilder m_stateBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	@Inject
	private TopReportService m_topReportService;

	@Inject
	private ExceptionRuleConfigManager m_configManager;

	private void buildExceptionDashboard(Model model, Payload payload, long date) {
		model.setReportStart(new Date(payload.getDate()));
		model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));

		int minuteCount = payload.getMinuteCounts();
		int minute = model.getMinute();
		TopReport report = queryTopReport(payload);

		List<String> excludeDomains = Arrays.asList(Constants.FRONT_END);
		TopMetric topMetric = new TopMetric(minuteCount, payload.getTopCounts(), m_configManager, excludeDomains);
		Date end = new Date(payload.getDate() + TimeHelper.ONE_MINUTE * minute);
		Date start = new Date(end.getTime() - TimeHelper.ONE_MINUTE * minuteCount);

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			Payload lastPayload = new Payload();
			Date lastHour = new Date(payload.getDate() - TimeHelper.ONE_HOUR);

			lastPayload.setDate(new SimpleDateFormat("yyyyMMddHH").format(lastHour));

			TopReport lastReport = queryTopReport(lastPayload);

			topMetric.visitTopReport(lastReport);
			model.setLastTopReport(lastReport);
		}
		topMetric.visitTopReport(report);
		model.setTopReport(report);
		model.setTopMetric(topMetric);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "top")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "top")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TOP);
		normalize(model, payload);
		long date = payload.getDate();

		buildExceptionDashboard(model, payload, date);
		model.setMessage(m_stateBuilder.buildStateMessage(payload.getDate(), payload.getIpAddress()));

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.TOP);
		model.setAction(Action.VIEW);
		m_normalizePayload.normalize(model, payload);

		Integer minute = parseQueryMinute(payload);
		int maxMinute = 60;
		List<Integer> minutes = new ArrayList<Integer>();

		if (payload.getPeriod().isCurrent()) {
			long current = payload.getCurrentTimeMillis() / 1000 / 60;
			maxMinute = (int) (current % (60));
		}
		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		model.setMinute(minute);
		model.setMaxMinute(maxMinute);
		model.setMinutes(minutes);
	}

	private int parseQueryMinute(Payload payload) {
		int minute = 0;
		String min = payload.getMinute();

		if (StringUtils.isEmpty(min)) {
			long current = payload.getCurrentTimeMillis() / 1000 / 60;
			minute = (int) (current % (60));
		} else {
			minute = Integer.parseInt(min);
		}

		return minute;
	}

	private TopReport queryTopReport(Payload payload) {
		String domain = Constants.CAT;
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = m_topReportService.queryReport(domain, new Date(payload.getDate()), new Date(payload.getDate()
				      + TimeHelper.ONE_HOUR));
			}
			report.accept(new TopExceptionExclude(m_configManager));
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}
}
