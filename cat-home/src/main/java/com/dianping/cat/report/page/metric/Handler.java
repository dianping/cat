package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.page.metric.MetricConfig.MetricFlag;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "metric")
	private ModelService<MetricReport> m_service;

	@Inject
	private NormalizePayload m_normalizePayload;

	private static final String TUAN = "TuanGou";

	private MetricConfig buildTuanGouMetricConfig(String channel) {
		MetricConfig config = new MetricConfig();

		MetricFlag indexUrl = new MetricFlag("/index", channel, 1, true, false, false, MetricTitle.INDEX);
		MetricFlag detailUrl = new MetricFlag("/detail", channel, 2, true, false, false, MetricTitle.DETAIL);
		MetricFlag payUrl = new MetricFlag("/order/submitOrder", channel, 3, true, false, false, MetricTitle.PAY);
		MetricFlag orderKey = new MetricFlag("order", channel, 4, false, true, false, MetricTitle.ORDER);
		MetricFlag totalKey = new MetricFlag("payment.success", channel, 5, false, true, false, MetricTitle.SUCCESS);
		// MetricFlag sumKey = new MetricFlag("payment.pending", 5, false, true, false);

		config.put(indexUrl);
		config.put(detailUrl);
		config.put(payUrl);
		config.put(orderKey);
		config.put(totalKey);
		return config;
	}

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
			MetricDisplay display = new MetricDisplay(buildTuanGouMetricConfig(channel), channel, startTime);

			display.visitMetricReport(report);
			model.setDisplay(display);
			model.setChannels(display.getAllChannel());
			model.setReport(report);
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		payload.setGroup(TUAN);
		model.setGroup(payload.getGroup());
		model.setChannel(payload.getChannel());
		model.setPage(ReportPage.METRIC);
		m_normalizePayload.normalize(model, payload);
	}

	public class MetricTitle {

		public static final String INDEX = "团购首页(次)";

		public static final String DETAIL = "团购详情(次)";

		public static final String PAY = "支付页面(次)";

		public static final String ORDER = "订单创建数量(个)";

		public static final String SUCCESS = "支付金额(元)";

	}

}
