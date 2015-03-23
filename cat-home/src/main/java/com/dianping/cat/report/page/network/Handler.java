package com.dianping.cat.report.page.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.network.nettopology.NetGraphManager;
import com.dianping.cat.report.service.ModelPeriod;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private GraphCreator m_graphCreator;

	@Inject
	private NetGraphManager m_netGraphManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "network")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "network")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		long date = payload.getDate();
		int timeRange = payload.getTimeRange();
		Date start = new Date(date - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date end = new Date(date + TimeHelper.ONE_HOUR);

		switch (payload.getAction()) {
		case METRIC:
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(payload.getProduct(), start, end);

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			break;
		case NETTOPOLOGY:
			model.setNetGraphData(m_netGraphManager.getNetGraphData(model.getStartTime(), model.getMinute()));
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		Collection<ProductLine> productLines = m_productLineConfigManager.queryNetworkProductLines().values();

		model.setProductLines(productLines);
		model.setPage(ReportPage.NETWORK);

		if (StringUtils.isEmpty(payload.getProduct()) && StringUtils.isEmpty(payload.getGroup())
		      && !productLines.isEmpty()) {
			payload.setProduct(productLines.iterator().next().getId());
		}

		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);

		if (payload.getAction().equals(Action.NETTOPOLOGY)) {
			long current = System.currentTimeMillis() - TimeHelper.ONE_MINUTE;
			int curMinute = (int) ((current - current % TimeHelper.ONE_MINUTE) % TimeHelper.ONE_HOUR / TimeHelper.ONE_MINUTE);
			long startTime = payload.getDate();
			int minute = payload.getMinute();

			if (minute == -1) {
				minute = curMinute;
				if (curMinute == 59) {
					startTime -= TimeHelper.ONE_HOUR;
				}
			}

			int maxMinute = 59;
			if (startTime == ModelPeriod.CURRENT.getStartTime()) {
				maxMinute = curMinute;
			}

			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeHelper.ONE_HOUR - 1);
			List<Integer> minutes = new ArrayList<Integer>();

			for (int i = 0; i < 60; i++) {
				minutes.add(i);
			}

			model.setMinutes(minutes);
			model.setMinute(minute);
			model.setMaxMinute(maxMinute);
			model.setStartTime(start);
			model.setEndTime(end);
			model.setIpAddress(payload.getIpAddress());
			model.setAction(payload.getAction());
			model.setDisplayDomain(payload.getDomain());
		} else {
			int timeRange = payload.getTimeRange();
			Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeHelper.ONE_HOUR);
			Date endTime = new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1);

			model.setStartTime(startTime);
			model.setEndTime(endTime);
		}
	}
}
