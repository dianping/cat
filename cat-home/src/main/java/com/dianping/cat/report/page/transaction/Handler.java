package com.dianping.cat.report.page.transaction;

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
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.DomainGroupConfigManager;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_historyGraph;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private XmlViewer m_xmlViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private TransactionMergeManager m_mergeManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private DomainGroupConfigManager m_configManager;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	private void buildTransactionMetaInfo(Model model, Payload payload, TransactionReport report) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String queryName = payload.getQueryName();
		String ip = payload.getIpAddress();

		if (!StringUtils.isEmpty(type)) {
			DisplayNames displayNames = new DisplayNames();

			model.setDisplayNameReport(displayNames.display(sorted, type, ip, report, queryName));
			buildTransactionNamePieChart(displayNames.getResults(), model);
		} else {
			model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, report));
		}
	}

	private void buildTransactionNameGraph(Model model, TransactionReport report, String type, String name, String ip) {
		TransactionType t = report.findOrCreateMachine(ip).findOrCreateType(type);
		TransactionName transactionName = t.findOrCreateName(name);

		if (transactionName != null) {
			String graph1 = m_builder.build(new DurationPayload("Duration Distribution", "Duration (ms)", "Count",
			      transactionName));
			String graph2 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", transactionName));
			String graph3 = m_builder.build(new AverageTimePayload("Average Duration Over Time", "Time (min)",
			      "Average Duration (ms)", transactionName));
			String graph4 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count",
			      transactionName));

			model.setGraph1(graph1);
			model.setGraph2(graph2);
			model.setGraph3(graph3);
			model.setGraph4(graph4);
		}
	}

	private void buildTransactionNamePieChart(List<TransactionNameModel> names, Model model) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (int i = 1; i < names.size(); i++) {
			TransactionNameModel name = names.get(i);
			Item item = new Item();
			TransactionName transaction = name.getDetail();
			item.setNumber(transaction.getTotalCount()).setTitle(transaction.getId());
			items.add(item);
		}

		chart.addItems(items);
		model.setPieChart(new JsonBuilder().toJson(chart));
	}

	private void calculateTps(Payload payload, TransactionReport report) {
		try {
			if (payload != null && report != null) {
				boolean isCurrent = payload.getPeriod().isCurrent();
				double seconds;

				if (isCurrent) {
					seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
				} else {
					if (report.getEndTime() != null && report.getStartTime() != null) {
						seconds = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
					} else {
						seconds = 60;
					}
				}
				new TpsStatistics(seconds).visitTransactionReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private TransactionReport filterReportByGroup(TransactionReport report, String domain, String group) {
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

	private TransactionReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()).setProperty("type", payload.getType())
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Date start = new Date(payload.getDate());
				Date end = new Date(payload.getDate() + TimeUtil.ONE_HOUR);

				if (Constants.ALL.equals(domain)) {
					report = m_reportService.queryTransactionReport(domain, start, end);
				}
				Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private TransactionReport getTransactionGraphReport(Model model, Payload payload) {
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
		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();

		return report;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
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
			TransactionReport report = getHourlyReport(payload);

			report = m_mergeManager.mergerAllIp(report, ipAddress);
			calculateTps(payload, report);
			if (report != null) {
				model.setReport(report);

				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_REPORT:
			report = m_reportService.queryTransactionReport(domain, payload.getHistoryStartDate(),
			      payload.getHistoryEndDate());
			calculateTps(payload, report);

			if (report != null) {
				model.setReport(report);
				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GRAPH:
			m_historyGraph.buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			report = getTransactionGraphReport(model, payload);

			if (name == null || name.length() == 0) {
				name = Constants.ALL;
			}
			report = m_mergeManager.mergerAllName(report, ip, name);
			model.setReport(report);
			buildTransactionNameGraph(model, report, type, name, ip);
			break;
		case HOURLY_GROUP_REPORT:
			report = getHourlyReport(payload);
			report = filterReportByGroup(report, domain, group);
			report = m_mergeManager.mergerAllIp(report, ipAddress);
			calculateTps(payload, report);
			if (report != null) {
				model.setReport(report);

				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case HISTORY_GROUP_REPORT:
			report = m_reportService.queryTransactionReport(domain, payload.getHistoryStartDate(),
			      payload.getHistoryEndDate());

			calculateTps(payload, report);
			report = filterReportByGroup(report, domain, group);
			report = m_mergeManager.mergerAllIp(report, ipAddress);
			if (report != null) {
				model.setReport(report);
				buildTransactionMetaInfo(model, payload, report);
			}
			break;
		case GROUP_GRAPHS:
			report = getTransactionGraphReport(model, payload);
			report = filterReportByGroup(report, domain, group);

			if (name == null || name.length() == 0) {
				name = Constants.ALL;
			}
			report = m_mergeManager.mergerAllName(report, ip, name);
			model.setReport(report);
			buildTransactionNameGraph(model, report, type, name, ip);
			break;
		case HISTORY_GROUP_GRAPH:
			List<String> ips = m_configManager.queryIpByDomainAndGroup(domain, group);

			m_historyGraph.buildGroupTrendGraph(model, payload, ips);
			break;
		}

		if (payload.isXml()) {
			m_xmlViewer.view(ctx, model);
		} else {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.TRANSACTION);
		m_normalizePayload.normalize(model, payload);

		if (StringUtils.isEmpty(payload.getQueryName())) {
			payload.setQueryName(null);
		}
		if (StringUtils.isEmpty(payload.getType())) {
			payload.setType(null);
		}

		String queryName = payload.getQueryName();
		if (queryName != null) {
			model.setQueryName(queryName);
		}
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}
}
