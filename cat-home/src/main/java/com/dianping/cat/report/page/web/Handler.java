package com.dianping.cat.report.page.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.Monitor;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.web.graph.WebGraphCreator;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private CityManager m_cityManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private WebGraphCreator m_graphCreator;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "web")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "web")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		Collection<PatternItem> rules = m_patternManager.queryUrlPatternRules();

		long start = payload.getHistoryStartDate().getTime();
		long end = payload.getHistoryEndDate().getTime();

		start = start - start % TimeHelper.ONE_HOUR;
		end = end - end % TimeHelper.ONE_HOUR;

		Date startDate = new Date(start);
		Date endDate = new Date(end);
		String type = payload.getType();
		String channel = payload.getChannel();
		String city = payload.getCity();
		Map<String, String> pars = new LinkedHashMap<String, String>();
		String url = payload.getUrl();

		if (url == null && rules.size() > 0) {
			PatternItem patternItem = new ArrayList<PatternItem>(rules).get(0);

			url = patternItem.getName();
			payload.setGroup(patternItem.getGroup());
			payload.setUrl(url);
		}

		pars.put("metricType", Constants.METRIC_USER_MONITOR);
		pars.put("type", type);
		pars.put("channel", channel);
		pars.put("city", city);

		if (url != null) {
			if (Monitor.TYPE_INFO.equals(type)) {
				Pair<Map<String, LineChart>, List<PieChart>> charts = m_graphCreator.queryBaseInfo(startDate, endDate, url,
				      pars);
				Map<String, LineChart> lineCharts = charts.getKey();
				List<PieChart> pieCharts = charts.getValue();

				model.setLineCharts(lineCharts);
				model.setPieCharts(pieCharts);
			} else {
				Pair<LineChart, PieChart> pair = m_graphCreator.queryErrorInfo(startDate, endDate, url, pars);

				model.setLineChart(pair.getKey());
				model.setPieChart(pair.getValue());
			}
		}
		model.setStart(startDate);
		model.setEnd(endDate);
		model.setPattermItems(rules);
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.WEB);
		model.setCityInfo(m_cityManager.getCityInfo());

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.WEB);

		m_normalizePayload.normalize(model, payload);
	}
}
