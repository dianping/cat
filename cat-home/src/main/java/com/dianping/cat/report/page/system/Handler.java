package com.dianping.cat.report.page.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.productline.ProductLineConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.system.graph.SystemGraphCreator;
import com.dianping.cat.service.ProjectService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private SystemGraphCreator m_graphCreator;

	@Inject
	private ProductLineConfigManager m_productLineManager;

	public List<Project> buildProjects() {
		List<Project> projects = new ArrayList<Project>();
		Map<String, Project> map = new HashMap<String, Project>();

		try {
			projects = m_projectService.findAll();
		} catch (DalException e) {
			Cat.logError(e);
		}
		for (Project p : projects) {
			map.put(p.getCmdbDomain(), p);
		}
		return new ArrayList<Project>(map.values());
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "system")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "system")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		Map<String, String> pars = new HashMap<String, String>();

		pars.put("ip", payload.getIpAddrs());
		pars.put("type", payload.getType());
		pars.put("metricType", Constants.METRIC_SYSTEM_MONITOR);
		normalize(model, payload);

		String domain = payload.getDomain();
		String productLine = m_productLineManager.querySystemProductLine(domain);

		long start = payload.getHistoryStartDate().getTime();
		long end = payload.getHistoryEndDate().getTime();
		start = start - start % TimeHelper.ONE_HOUR;
		end = end - end % TimeHelper.ONE_HOUR;
		Date startDate = new Date(start);
		Date endDate = new Date(end);

		model.setStartTime(startDate);
		model.setEndTime(endDate);

		switch (action) {
		case SYSTEM:
			Set<String> ipAddrs = new HashSet<String>();
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(productLine, pars, ipAddrs, startDate,
			      endDate);

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			model.setIpAddrs(ipAddrs);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setProjects(buildProjects());
		model.setPage(ReportPage.SYSTEM);
		m_normalizePayload.normalize(model, payload);
	}
}
