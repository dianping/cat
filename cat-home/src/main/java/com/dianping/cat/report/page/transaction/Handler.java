package com.dianping.cat.report.page.transaction;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.transaction.StatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;
import com.dianping.cat.report.page.trend.GraphItem;
import com.google.gson.Gson;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private DailyreportDao dailyreportDao;

	private StatisticsComputer m_computer = new StatisticsComputer();

	private DefaultDomParser transactionParser = new DefaultDomParser();

	private Gson gson = new Gson();

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
			setTps(payload, report);
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	private void setTps(Payload payload, TransactionReport report) {
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
					tps = totalCount / (double) 3600;
				}
				transType.setTps(tps);
				for (TransactionName transName : transType.getNames().values()) {
					long totalNameCount = transName.getTotalCount();
					double nameTps = 0;
					if (isCurrent) {
						double seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
						nameTps = totalNameCount / seconds;
					} else {
						nameTps = totalNameCount / (double) 3600;
					}
					transName.setTps(nameTps);
				}
			}
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
		switch (payload.getAction()) {
		case HOURLY_REPORT:
			showHourlyReport(model, payload);
			break;
		case HISTORY_REPORT:
			showSummarizeReport(model, payload);
			break;
		case HITORY_GRAPH:
			buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			showComputerGraphs(model, payload);
			break;
		case MOBILE:
			showHourlyReport(model, payload);
			if (!StringUtils.isEmpty(payload.getType())) {
				DisplayTransactionNameReport report = model.getDisplayNameReport();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			} else {
				DisplayTransactionTypeReport report = model.getDisplayTypeReport();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			}
			break;
		case MOBILE_GRAPHS:
			MobileTransactionGraphs graphs = showMobileGraphs(model, payload);
			if (graphs != null) {
				model.setMobileResponse(gson.toJson(graphs));
			}
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String domain = model.getDomain();
		String ip = model.getIpAddress();
		String type = model.getType();
		String name = payload.getName();
		
		long current = System.currentTimeMillis();
		current = current - current % (3600 * 1000);

		long date = current - 24 * 3600 * 1000;
		start = new Date(date);
		end = new Date(current);
		int size = (int) (current - date) / (3600 * 1000);

		GraphItem item = new GraphItem();
		item.setStart(start);
		item.setSize(size);

		//TO GET The Data from database
		//TODO
		// For URL
		item.setTitles(" URL Response Trend");
		double[] ylable1 = new double[size];
		for (int i = 0; i < size; i++) {
			//TODO
			ylable1[i] = Math.random() * 192;
		}
		item.addValue(ylable1);
		model.setResponseTrend(item.getJsonString());

		item.setTitles(" URL Hit Trend");
		item.getValues().clear();
		ylable1 = new double[size];
		for (int i = 0; i < size; i++) {
			ylable1[i] = Math.random() * 192;
		}
		item.addValue(ylable1);
		model.setHitTrend(item.getJsonString());

	}

	private void showSummarizeReport(Model model, Payload payload) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIpAddress();
		if (ip == null) {
			ip = CatString.ALL_IP;
		}
		model.setIpAddress(ip);

		TransactionReport transactionReport = null;
		try {
			Date start = payload.getHistoryStartDate();
			Date end = payload.getHistoryEndDate();
			String domain = model.getDomain();
			List<Dailyreport> reports = dailyreportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
			      DailyreportEntity.READSET_FULL);
			TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
			for (Dailyreport report : reports) {
				String xml = report.getContent();
				TransactionReport reportModel = transactionParser.parse(xml);
				reportModel.accept(merger);
			}
			transactionReport = merger == null ? null : merger.getTransactionReport();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (transactionReport == null) {
			return;
		}
		model.setReport(transactionReport);
		if (!StringUtils.isEmpty(type)) {
			model.setDisplayNameReport(new DisplayTransactionNameReport().display(sorted, type, ip, transactionReport, ""));
		} else {
			model.setDisplayTypeReport(new DisplayTransactionTypeReport().display(sorted, ip, transactionReport));
		}
	}

	public void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.TRANSACTION);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		String ip = payload.getIpAddress();

		if (ip == null || ip.length() == 0) {
			payload.setIpAddress(CatString.ALL_IP);
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
		if (action == Action.HISTORY_REPORT||action==Action.HITORY_GRAPH) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());
		}
	}

	private MobileTransactionGraphs showMobileGraphs(Model model, Payload payload) {
		TransactionName name = getTransactionName(payload);

		if (name == null) {
			return null;
		}
		MobileTransactionGraphs graphs = new MobileTransactionGraphs().display(name);
		return graphs;
	}

	private void showComputerGraphs(Model model, Payload payload) {
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
				if (queryName != null) {
					model.setQueryName(queryName);
				}
				if (!StringUtils.isEmpty(type)) {
					model.setDisplayNameReport(new DisplayTransactionNameReport().display(sorted, type, ip, report,
					      queryName));
				} else {
					model.setDisplayTypeReport(new DisplayTransactionTypeReport().display(sorted, ip, report));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();

			Cat.getProducer().logError(e);
			model.setException(e);
		}
	}

	
	public Map<String,double[]> getDetailInfo(Model model,Payload payload){
		Date start = payload.getHistoryEndDate();
		Date end = payload.getHistoryEndDate();
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ip = model.getIpAddress();
		return null;
	}
	
}
