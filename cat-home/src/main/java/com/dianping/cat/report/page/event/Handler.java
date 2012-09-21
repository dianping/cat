package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.event.EventMerger;
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
	private HistoryGraphs m_eventHistoryGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private EventMerger m_eventMerger;

	@Inject
	protected ReportDao m_reportDao;

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_service;

	private StatisticsComputer m_computer = new StatisticsComputer();

	private void calculateTps(Payload payload, EventReport report) {
		if (payload != null && report != null) {
			boolean isCurrent = payload.getPeriod().isCurrent();
			String ip = payload.getIpAddress();
			Machine machine = report.getMachines().get(ip);
			if (machine == null) {
				return;
			}
			for (EventType eventType : machine.getTypes().values()) {
				long totalCount = eventType.getTotalCount();
				double tps = 0;
				if (isCurrent) {
					double seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
					tps = totalCount / seconds;
				} else {
					double time = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
					tps = totalCount / (double) time;
				}
				eventType.setTps(tps);
				for (EventName transName : eventType.getNames().values()) {
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
					transName.setTotalPercent(totalNameCount / (double) totalCount);
				}
			}
		}
	}

	private EventName getEventName(Payload payload) {
		String domain = payload.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ipAddress = payload.getIpAddress();
		String date = String.valueOf(payload.getDate());
		String ip = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType())//
		      .setProperty("name", payload.getName())//
		      .setProperty("ip", ipAddress);
		if (name == null || name.length() == 0) {
			request.setProperty("name", "*");
			request.setProperty("all", "true");
			name = "ALL";
		}
		ModelResponse<EventReport> response = m_service.invoke(request);
		EventReport report = response.getModel();
		EventType t = report.getMachines().get(ip).findType(type);

		if (t != null) {
			EventName n = t.findName(name);

			if (n != null) {
				n.accept(m_computer);
			}
			return n;
		}
		return null;
	}

	private EventReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("type", payload.getType())//
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<EventReport> response = m_service.invoke(request);
			EventReport report = response.getModel();
			calculateTps(payload, report);
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

		switch (payload.getAction()) {
		case HOURLY_REPORT:
			showHourlyReport(model, payload);
			break;
		case HISTORY_REPORT:
			showSummarizeReport(model, payload);
			break;
		case HISTORY_GRAPH:
			m_eventHistoryGraphs.buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			showGraphs(model, payload);
			break;
		case MOBILE:
			showHourlyReport(model, payload);
			if (!StringUtils.isEmpty(payload.getType())) {
				DisplayNames report = model.getDisplayNameReport();
				Gson gson = new Gson();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			} else {
				DisplayTypes report = model.getDisplayTypeReport();
				Gson gson = new Gson();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			}
			break;
		case MOBILE_GRAPHS:
			MobileGraphs graphs = showMobileGraphs(model, payload);
			if (graphs != null) {
				Gson gson = new Gson();
				model.setMobileResponse(gson.toJson(graphs));
			}
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	public void normalize(Model model, Payload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		String ip = payload.getIpAddress();
		if (StringUtils.isEmpty(ip)) {
			payload.setIpAddress(CatString.ALL_IP);
		}
		if (StringUtils.isEmpty(payload.getType())) {
			payload.setType(null);
		}
		model.setIpAddress(payload.getIpAddress());
		model.setAction(payload.getAction());
		model.setPage(ReportPage.EVENT);
		model.setDisplayDomain(payload.getDomain());
		if (payload.getPeriod().isCurrent()) {
			model.setCreatTime(new Date());
		} else {
			model.setCreatTime(new Date(payload.getDate() + 60 * 60 * 1000 - 1000));
		}
		if (payload.getAction() == Action.HISTORY_REPORT || payload.getAction() == Action.HISTORY_GRAPH) {
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

	private void showGraphs(Model model, Payload payload) {
		EventName name = getEventName(payload);

		if (name == null) {
			return;
		}

		String graph1 = m_builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", name));
		String graph2 = m_builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", name));

		model.setGraph1(graph1);
		model.setGraph2(graph2);
	}

	private void showHourlyReport(Model model, Payload payload) {
		try {
			EventReport report = getReport(payload);

			if (payload.getPeriod().isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}

			if (report != null) {
				report.accept(m_computer);
				model.setReport(report);
			}

			String type = payload.getType();
			String sorted = payload.getSortBy();
			String ip = payload.getIpAddress();

			if (!StringUtils.isEmpty(type)) {
				model.setDisplayNameReport(new DisplayNames().display(sorted, type, ip, report));
				buildEventNameGraph(ip, type, report, model);
			} else {
				model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, payload.isShowAll(), report));
			}
		} catch (Throwable e) {
			Cat.logError(e);
			model.setException(e);
		}
	}

	private void buildEventNameGraph(String ip, String type, EventReport report, Model model) {
		PieChart chart = new PieChart();
		Collection<EventName> values = report.findOrCreateMachine(ip).findOrCreateType(type).getNames().values();
		List<Item> items = new ArrayList<Item>();
		for (EventName name : values) {
			Item item = new Item();
			item.setNumber(name.getTotalCount()).setTitle(name.getId());
			items.add(item);
		}

		chart.setItems(items);
		Gson gson = new Gson();
		model.setPieChart(gson.toJson(chart));
	}

	private MobileGraphs showMobileGraphs(Model model, Payload payload) {
		EventName name = getEventName(payload);

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

		EventReport eventReport = null;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "event",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findAllByDomainNameDuration(start, end, null, "event",
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domains = new HashSet<String>();
				for (Report report : allReports) {
					domains.add(report.getDomain());
				}
				eventReport = m_eventMerger.mergeForDaily(domain, reports, domains);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} else {
			try {
				List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "event",
				      DailyreportEntity.READSET_FULL);
				EventReportMerger merger = new EventReportMerger(new EventReport(domain));
				for (Dailyreport report : reports) {
					String xml = report.getContent();
					EventReport reportModel = DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				eventReport = merger.getEventReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		if (eventReport == null) {
			return;
		}
		eventReport.setStartTime(start);
		eventReport.setEndTime(end);
		eventReport.setDomain(model.getDisplayDomain());
		calculateTps(payload, eventReport);
		model.setReport(eventReport);
		if (!StringUtils.isEmpty(type)) {
			model.setDisplayNameReport(new DisplayNames().display(sorted, type, ip, eventReport));
			buildEventNameGraph(ip, type, eventReport, model);
		} else {
			model.setDisplayTypeReport(new DisplayTypes().display(sorted, ip, payload.isShowAll(), eventReport));
		}
	}

	public enum DetailOrder {
		TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT
	}

	public enum SummaryOrder {
		TYPE, TOTAL_COUNT, FAILURE_COUNT
	}
}
