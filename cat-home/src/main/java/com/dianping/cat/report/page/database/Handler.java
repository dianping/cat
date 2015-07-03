package com.dianping.cat.report.page.database;

import java.io.IOException;
import java.util.ArrayList;
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

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private GraphCreator m_graphCreator;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "database")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "database")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		long date = payload.getDate();
		int timeRange = payload.getTimeRange();
		Date start = new Date(date - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date end = new Date(date + TimeHelper.ONE_HOUR);

		switch (payload.getAction()) {
		case VIEW:
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(payload.getGroup(),
			      payload.getProduct(), start, end);

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			model.setGroups(DatabaseGroup.KEY_GROUPS.keySet());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		List<ProductLine> databases = new ArrayList<ProductLine>(m_productLineConfigManager.queryDatabaseProductLines()
		      .values());

		model.setPage(ReportPage.DATABASE);
		model.setProductLines(databases);
		model.setAction(payload.getAction());

		m_normalizePayload.normalize(model, payload);

		if (StringUtils.isEmpty(payload.getProduct())) {
			if (databases.size() > 0) {
				payload.setProduct(databases.get(0).getId());
			} else {
				payload.setProduct("Default");
			}
		}
		
		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);
	}

}
