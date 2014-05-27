package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.event.DisplayNames.EventNameModel;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.DomainGroupConfigManager;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private EventMergeManager m_mergeManager;

	@Inject(type = ModelService.class, value = EventAnalyzer.ID)
	private ModelService<EventReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private DomainGroupConfigManager m_configManager;

	private void buildEventMetaInfo(Model model, Payload payload, EventReport report) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIpAddress();

		if (!StringUtils.isEmpty(type)) {
			DisplayNames displayNames = new DisplayNames();

			model.setDisplayNameReport(displayNames.display(sorted, type, ip, report));
			buildEventNamePieChart(displayNames.getResults(), model);
		} else {
			model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, payload.isShowAll(), report));
		}
	}

	private void buildEventNameGraph(Model model, EventReport report, String type, String name, String ip) {
		EventType t = report.findOrCreateMachine(ip).findOrCreateType(type);
		EventName eventName = t.findOrCreateName(name);

		if (eventName != null) {
			String graph1 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", eventName));
			String graph2 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", eventName));

			model.setGraph1(graph1);
			model.setGraph2(graph2);
		}
	}

	private void buildEventNamePieChart(List<EventNameModel> names, Model model) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (int i = 1; i < names.size(); i++) {
			EventNameModel name = names.get(i);
			Item item = new Item();
			EventName event = name.getDetail();
			item.setNumber(event.getTotalCount()).setTitle(event.getId());
			items.add(item);
		}

		chart.addItems(items);
		model.setPieChart(new JsonBuilder().toJson(chart));
	}

	private void calculateTps(Payload payload, EventReport report) {
		try {
			if (payload != null && report != null) {
				boolean isCurrent = payload.getPeriod().isCurrent();
				double seconds;
				if (isCurrent) {
					seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
				} else {
					seconds = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
				}
				new TpsStatistics(seconds).visitEventReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private EventReport filterReportByGroup(EventReport report, String domain, String group) {
		List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);
		List<String> removes = new ArrayList<String>();

		for (Machine machine : report.getMachines().values()) {
			String ip = machine.getIp();

			if (!ips.contains(ip)) {
				removes.add(ip);
			}
		}
		for (String ip : removes) {
			report.getMachines().remove(ip);
		}
		return report;
	}

	private EventReport getEventGraphReport(Model model, Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String name = payload.getName();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("type", payload.getType()) //
		      .setProperty("name", payload.getName())//
		      .setProperty("ip", ipAddress);

		if (name == null || name.length() == 0) {
			request.setProperty("name", "*");
			request.setProperty("all", "true");
			name = Constants.ALL;
		}
		ModelResponse<EventReport> response = m_service.invoke(request);
		EventReport report = response.getModel();

		return report;
	}

	private EventReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("type", payload.getType())//
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<EventReport> response = m_service.invoke(request);
			EventReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), EventAnalyzer.ID);
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable event service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "e")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "e")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		String domain = payload.getDomain();
		Action action = payload.getAction();
		String ipAddress = payload.getIpAddress();
		String group = payload.getGroup();
		String type = payload.getType();
		String name = payload.getName();
		String ip = payload.getIpAddress();

		if (StringUtils.isEmpty(group)) {
			group = m_configManager.queryDefaultGroup(domain);
			payload.setGroup(group);
		}
		model.setGroupIps(m_configManager.queryIpByDomainAndGroup(domain, group));
		model.setGroups(m_configManager.queryDomainGroup(payload.getDomain()));
		switch (action) {
		case HOURLY_REPORT:
			EventReport report = getHourlyReport(payload);

			report = m_mergeManager.mergerAllIp(report, ipAddress);
			calculateTps(payload, report);
			if (report != null) {
				model.setReport(report);

				buildEventMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_REPORT:
			report = m_reportService.queryEventReport(domain, payload.getHistoryStartDate(), payload.getHistoryEndDate());
			calculateTps(payload, report);

			if (report != null) {
				model.setReport(report);
				buildEventMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GRAPH:
			m_historyGraphs.buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			report = getEventGraphReport(model, payload);
			report = m_mergeManager.mergerAllIp(report, ipAddress);

			if (name == null || name.length() == 0) {
				name = Constants.ALL;
				report = m_mergeManager.mergerAllName(report, ip, name);
			}

			model.setReport(report);
			buildEventNameGraph(model, report, type, name, ip);
			break;
		case HOURLY_GROUP_REPORT:
			report = getHourlyReport(payload);
			report = filterReportByGroup(report, domain, group);
			report = m_mergeManager.mergerAllIp(report, ipAddress);
			calculateTps(payload, report);
			if (report != null) {
				model.setReport(report);

				buildEventMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GROUP_REPORT:
			report = m_reportService.queryEventReport(domain, payload.getHistoryStartDate(), payload.getHistoryEndDate());

			calculateTps(payload, report);
			report = filterReportByGroup(report, domain, group);
			report = m_mergeManager.mergerAllIp(report, ipAddress);

			if (report != null) {
				model.setReport(report);
				buildEventMetaInfo(model, payload, report);
			}
			break;
		case GROUP_GRAPHS:
			report = getEventGraphReport(model, payload);
			report = filterReportByGroup(report, domain, group);

			if (name == null || name.length() == 0) {
				name = Constants.ALL;
			}
			report = m_mergeManager.mergerAllName(report, ip, name);
			model.setReport(report);
			buildEventNameGraph(model, report, type, name, ip);
			break;
		case HISTORY_GROUP_GRAPH:
			List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);

			m_historyGraphs.buildGroupTrendGraph(model, payload, ips);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.EVENT);
		m_normalizePayload.normalize(model, payload);

		if (StringUtils.isEmpty(payload.getType())) {
			payload.setType(null);
		}
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT
	}
}
