package com.dianping.cat.report.page.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.chart.SystemGraphCreator;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private SystemGraphCreator m_graphCreator;

	@Inject
	private ProductLineConfigManager m_productLineManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "system")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	public String buildProject2Domains() {
		List<Project> projects = new ArrayList<Project>();
		Map<String, List<Project>> project2Domains = new HashMap<String, List<Project>>();

		try {
			projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
		} catch (DalException e) {
			e.printStackTrace();
		}
		for (Project project : projects) {
			String projectLine = project.getProjectLine();

			if (project2Domains.get(projectLine) == null) {
				project2Domains.put(projectLine, new ArrayList<Project>());
			}
			project2Domains.get(projectLine).add(project);
		}
		return new JsonBuilder().toJson(project2Domains);
	}

	@Override
	@OutboundActionMeta(name = "system")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		long start = payload.getHistoryStartDate().getTime();
		long end = payload.getHistoryEndDate().getTime();

		start = start - start % TimeUtil.ONE_HOUR;
		end = end - end % TimeUtil.ONE_HOUR;

		Date startDate = new Date(start);
		Date endDate = new Date(end);
		String domain = payload.getDomain();
		String productLine = m_productLineManager.querySystemProductLineByDomain(domain);
		model.setStartTime(startDate);
		model.setEndTime(endDate);
		model.setProjectsInfo(buildProject2Domains());
		
		switch (action) {
		case SYSTEM:
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(productLine, startDate, endDate);
			Set<String> ipAddrs = m_graphCreator.getAllIpAddrs();

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			model.setIpAddrs(ipAddrs);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		String curIpAddrStr = payload.getIpAddrs();

		if (curIpAddrStr != null && !curIpAddrStr.equals("All")) {
			String[] curIpAddrsArray = curIpAddrStr.split("_");
			Set<String> curIpAddrs = new HashSet<String>(Arrays.asList(curIpAddrsArray));

			m_graphCreator.setCurIpAddrs(curIpAddrs);
		} else {
			m_graphCreator.setCurIpAddrs(m_graphCreator.getAllIpAddrs());
		}

		String type = payload.getType();

		if (type != null && !type.isEmpty()) {
			m_graphCreator.setType(payload.getType());
		}

		model.setPage(ReportPage.SYSTEM);
		m_normalizePayload.normalize(model, payload);
	}
}
