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
import com.dianping.cat.consumer.transaction.TransactionStatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.NormalizePayload;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;
import com.dianping.cat.report.service.ReportService;
import com.google.gson.Gson;

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
	private NormalizePayload m_normalizePayload;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	private TransactionStatisticsComputer m_computer = new TransactionStatisticsComputer();

	private Gson m_gson = new Gson();

	private void buildTransactionNameGraph(List<TransactionNameModel> names, Model model) {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (int i = 1; i < names.size(); i++) {
			TransactionNameModel name = names.get(i);
			Item item = new Item();
			TransactionName transaction = name.getDetail();
			item.setNumber(transaction.getTotalCount()).setTitle(transaction.getId());
			items.add(item);
		}

		chart.setItems(items);
		Gson gson = new Gson();
		model.setPieChart(gson.toJson(chart));
	}

	private void calculateTps(Payload payload, TransactionReport report) {
		try {
			if (payload != null && report != null) {
				boolean isCurrent = payload.getPeriod().isCurrent();
				String ip = payload.getIpAddress();
				Machine machine = report.getMachines().get(ip);
				if (machine == null) {
					return;
				}
				for (TransactionType transType : machine.getTypes().values()) {
					long totalCount = transType.getTotalCount();
					double tps = 0;
					if (isCurrent) {
						double seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
						tps = totalCount / seconds;
					} else {
						double time = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
						tps = totalCount / (double) time;
					}
					transType.setTps(tps);
					for (TransactionName transName : transType.getNames().values()) {
						long totalNameCount = transName.getTotalCount();
						double nameTps = 0;
						if (isCurrent) {
							double seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
							nameTps = totalNameCount / seconds;
						} else {
							double time = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
							nameTps = totalNameCount / (double) time;
						}
						transName.setTps(nameTps);
						transName.setTotalPercent((double) totalNameCount / totalCount);
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private TransactionReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType())//
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Date start = new Date(payload.getDate());
				Date end = new Date(payload.getDate() + TimeUtil.ONE_HOUR);

				if (CatString.ALL.equals(domain)) {
					report = m_reportService.queryTransactionReport(domain, start, end);
				}
				Set<String> domains = m_reportService.queryAllDomainNames(start, end, "transaction");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			report = m_mergeManager.mergerAllIp(report, ipAddress);
			calculateTps(payload, report);
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private TransactionName getTransactionName(Payload payload) {
		String domain = payload.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ip = payload.getIpAddress();
		String ipAddress = payload.getIpAddress();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType()) //
		      .setProperty("name", payload.getName())//
		      .setProperty("ip", ipAddress);
		if (name == null || name.length() == 0) {
			request.setProperty("name", "*");
			request.setProperty("all", "true");
			name = CatString.ALL;
		}
		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();

		report = m_mergeManager.mergerAll(report, ipAddress, name);
		TransactionType t = report.getMachines().get(ip).findType(type);
		if (t != null) {
			TransactionName n = t.findName(name);
			if (n != null) {
				n.accept(m_computer);
			}
			return n;
		} else {
			return null;
		}
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
		String type = payload.getType();

		switch (payload.getAction()) {
		case HOURLY_REPORT:
			showHourlyReport(model, payload);
			DisplayNames displayNameReport = model.getDisplayNameReport();
			if ((!StringUtils.isEmpty(type)) && displayNameReport != null) {
				buildTransactionNameGraph(displayNameReport.getResults(), model);
			}
			break;
		case HISTORY_REPORT:
			showSummarizeReport(model, payload);
			displayNameReport = model.getDisplayNameReport();
			if ((!StringUtils.isEmpty(type)) && displayNameReport != null) {
				buildTransactionNameGraph(displayNameReport.getResults(), model);
			}
			break;
		case HISTORY_GRAPH:
			m_historyGraph.buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			showHourlyGraphs(model, payload);
			break;
		case MOBILE:
			showHourlyReport(model, payload);
			if (!StringUtils.isEmpty(payload.getType())) {
				DisplayNames report = model.getDisplayNameReport();
				String json = m_gson.toJson(report);
				model.setMobileResponse(json);
			} else {
				DisplayTypes report = model.getDisplayTypeReport();
				String json = m_gson.toJson(report);
				model.setMobileResponse(json);
			}
			break;
		case MOBILE_GRAPHS:
			MobileGraphs graphs = showMobileGraphs(model, payload);
			if (graphs != null) {
				model.setMobileResponse(m_gson.toJson(graphs));
			}
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

	private void showHourlyGraphs(Model model, Payload payload) {
		TransactionName name = getTransactionName(payload);

		if (name == null) {
			return;
		}

		String graph1 = m_builder.build(new DurationPayload("Duration Distribution", "Duration (ms)", "Count", name));
		String graph2 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", name));
		String graph3 = m_builder.build(new AverageTimePayload("Average Duration Over Time", "Time (min)",
		      "Average Duration (ms)", name));
		String graph4 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", name));

		model.setGraph1(graph1);
		model.setGraph2(graph2);
		model.setGraph3(graph3);
		model.setGraph4(graph4);
	}

	private void showHourlyReport(Model model, Payload payload) {
		try {
			TransactionReport report = getHourlyReport(payload);

			if (report != null) {
				report.accept(m_computer);
				model.setReport(report);

				String type = payload.getType();
				String sorted = payload.getSortBy();
				String queryName = payload.getQueryName();
				String ip = payload.getIpAddress();
				if (!StringUtils.isEmpty(type)) {
					model.setDisplayNameReport(new DisplayNames().display(sorted, type, ip, report, queryName));
				} else {
					model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, report));
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
			model.setException(e);
		}
	}

	private MobileGraphs showMobileGraphs(Model model, Payload payload) {
		TransactionName name = getTransactionName(payload);

		if (name == null) {
			return null;
		}
		MobileGraphs graphs = new MobileGraphs().display(name);
		return graphs;
	}

	private void showSummarizeReport(Model model, Payload payload) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIpAddress();
		String domain = model.getDomain();

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		TransactionReport transactionReport = m_reportService.queryTransactionReport(domain, start, end);
		calculateTps(payload, transactionReport);
		model.setReport(transactionReport);
		if (transactionReport != null) {
			if (!StringUtils.isEmpty(type)) {
				model.setDisplayNameReport(new DisplayNames().display(sorted, type, ip, transactionReport,
				      payload.getQueryName()));
			} else {
				model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, transactionReport));
			}
		}
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}
}
