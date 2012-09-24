package com.dianping.cat.report.page.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.transaction.StatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.transaction.TransactionMerger;
import com.google.gson.Gson;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private HistoryGraphs m_historyGraph;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private XmlViewer m_xmlViewer;

	@Inject
	private TransactionMerger m_transactionMerger;

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	private ServerConfigManager m_manager;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	private StatisticsComputer m_computer = new StatisticsComputer();

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
			name = "ALL";
		}
		ModelResponse<TransactionReport> response = m_service.invoke(request);
		TransactionReport report = response.getModel();
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
			if (!StringUtils.isEmpty(type)) {
				buildTransactionNameGraph(model.getDisplayNameReport().getResults(), model);
			}

			break;
		case HISTORY_REPORT:
			showSummarizeReport(model, payload);
			if (!StringUtils.isEmpty(type)) {
				buildTransactionNameGraph(model.getDisplayNameReport().getResults(), model);
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
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.TRANSACTION);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		if (StringUtils.isEmpty(payload.getQueryName())) {
			payload.setQueryName(null);
		}
		if (StringUtils.isEmpty(payload.getType())) {
			payload.setType(null);
		}

		String ip = payload.getIpAddress();
		String queryName = payload.getQueryName();

		if (ip == null || ip.length() == 0) {
			payload.setIpAddress(CatString.ALL_IP);
		}
		if (queryName != null) {
			model.setQueryName(queryName);
		}
		model.setIpAddress(payload.getIpAddress());
		model.setDisplayDomain(payload.getDomain());

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		if (payload.getPeriod().isCurrent()) {
			model.setCreatTime(new Date());
		} else {
			model.setCreatTime(new Date(payload.getDate() + 60 * 60 * 1000 - 1000));
		}
		if (action == Action.HISTORY_REPORT || action == Action.HISTORY_GRAPH) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			if (!payload.isToday()) {
				payload.setYesterdayDefault();
			}
			model.setLongDate(payload.getDate());
			model.setCustomDate(payload.getHistoryStartDate(), payload.getHistoryEndDate());
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

		TransactionReport transactionReport = null;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findAllByDomainNameDuration(start, end, null, "transaction",
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domains = new HashSet<String>();
				for (Report report : allReports) {
					domains.add(report.getDomain());
				}
				transactionReport = m_transactionMerger.mergeForDaily(domain, reports, domains);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} else {
			try {
				List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
				      DailyreportEntity.READSET_FULL);
				TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
				for (Dailyreport report : reports) {
					String xml = report.getContent();
					TransactionReport reportModel = DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				transactionReport = merger.getTransactionReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		if (transactionReport == null) {
			return;
		}
		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);
		calculateTps(payload, transactionReport);
		model.setReport(transactionReport);
		if (!StringUtils.isEmpty(type)) {
			model.setDisplayNameReport(new DisplayNames().display(sorted, type, ip, transactionReport,
			      payload.getQueryName()));
		} else {
			model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, transactionReport));
		}
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
	}
}
