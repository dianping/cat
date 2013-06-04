package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.advanced.BussinessConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "metric")
	private ModelService<MetricReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private BussinessConfigManager m_configManager;

	private static final String TUAN = "TuanGou";

	private MetricReport getReport(Payload payload) {
		String group = payload.getGroup();
		String channel = payload.getChannel();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(group, payload.getPeriod()) //
		      .setProperty("date", date);

		if (channel != null) {
			request.setProperty("channel", channel);
		}
		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "metric")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "metric")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		
		normalize(model, payload);

		MetricReport report = getReport(payload);
		String channel = payload.getChannel();

		if (report != null) {
			Date startTime = report.getStartTime();
			if (startTime == null) {
				startTime = payload.getHistoryStartDate();
			}
			MetricDisplay display = new MetricDisplay(m_configManager.getConfigs(payload.getGroup()), channel, startTime);

			display.visitMetricReport(report);
			model.setDisplay(display);
			model.setChannels(display.getAllChildKeyValues());//TODO
			model.setReport(report);
			model.setGroups(m_configManager.getGroups());
			model.setChildKey(display.getChildKey());
			model.setChildKeyValues(display.getAllChildKeyValues());
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setChannel(payload.getChannel());
		model.setPage(ReportPage.METRIC);
		m_normalizePayload.normalize(model, payload);

		String group = payload.getGroup();
		if (group == null || group.length() == 0) {
			payload.setGroup(TUAN);
		}
		model.setGroup(payload.getGroup());
	}

}
