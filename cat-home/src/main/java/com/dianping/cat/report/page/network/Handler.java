package com.dianping.cat.report.page.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.network.influx.InfluxNetGraphManager;
import com.dianping.cat.report.service.ModelPeriod;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private InfluxNetGraphManager m_influxNetGraphManager;

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

		switch (payload.getAction()) {
		case DASHBOARD:
			model.setNetGraphData(m_influxNetGraphManager.getNetGraphData(model.getStartTime(), model.getMinute()));
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.NETWORK);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);

		switch (payload.getAction()) {
		case DASHBOARD:
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
			break;
		}
	}
}
