package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.google.gson.Gson;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_eventHistoryGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ReportService m_reportService;

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

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		
		EventReport eventReport = m_reportService.queryEventReport(domain, start, end);
		
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
