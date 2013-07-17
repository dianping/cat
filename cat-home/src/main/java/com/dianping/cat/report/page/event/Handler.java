package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_eventHistoryGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private EventMergeManager m_mergeManager;

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

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
		model.setPieChart(new Gson().toJson(chart));
	}

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
		String ip = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("type", payload.getType())//
		      .setProperty("name", payload.getName())//
		      .setProperty("ip", ipAddress);
		if (name == null || name.length() == 0) {
			request.setProperty("name", "*");
			request.setProperty("all", "true");
			name = CatString.ALL;
		}
		ModelResponse<EventReport> response = m_service.invoke(request);
		EventReport report = response.getModel();
		report = m_mergeManager.mergerAll(report, ipAddress, name);

		EventType t = report.getMachines().get(ip).findType(type);

		if (t != null) {
			return t.findName(name);
		}
		return null;
	}

	private EventReport getReport(Payload payload) {
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
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), "event");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			report = m_mergeManager.mergerAllIp(report, ipAddress);
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
