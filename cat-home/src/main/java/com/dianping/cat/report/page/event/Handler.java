package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
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

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	@Inject
	private ServerConfigManager m_manager;
	
	@Inject
	private DailyreportDao dailyreportDao;

	private StatisticsComputer m_computer = new StatisticsComputer();

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
			showSummarizeReport(model,payload);
			break;
		case HISTORY_GRAPH:
			buildTrendGraph(model, payload);
			break;
		case GRAPHS:
			showGraphs(model, payload);
			break;
		case MOBILE:
			showHourlyReport(model, payload);
			if (!StringUtils.isEmpty(payload.getType())) {
				DisplayEventNameReport report = model.getDisplayNameReport();
				Gson gson = new Gson();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			} else {
				DisplayEventTypeReport report = model.getDisplayTypeReport();
				Gson gson = new Gson();
				String json = gson.toJson(report);
				model.setMobileResponse(json);
			}
			break;
		case MOBILE_GRAPHS:
			MobileEventGraphs graphs = showMobileGraphs(model, payload);
			if (graphs != null) {
				Gson gson = new Gson();
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
		String type = payload.getType();
		String name = payload.getName();
		String display = name != null ? name : type;
		System.out.println(start +" "+end +" "+domain+" "+ip +" "+ type +" "+name);
		long current = System.currentTimeMillis();
		current = current - current % (3600 * 1000);

		long date = current - 24 * 3600 * 1000;
		start = new Date(date);
		end = new Date(current);
		int size = (int) (current - date) / (3600 * 1000);

		GraphItem item = new GraphItem();
		item.setStart(start);
		item.setSize(size);

		// TO GET The Data from database
		// TODO
		// For URL
		item.setTitles(display + " Response Trend");
		double[] ylable1 = new double[size];
		item.setTitles(display + " Hit Trend");
		ylable1 = new double[size];
		for (int i = 0; i < size; i++) {
			ylable1[i] = Math.random() * 192;
		}
		item.addValue(ylable1);
		model.setHitTrend(item.getJsonString());	   
   }

	private com.dianping.cat.consumer.event.model.transform.DefaultDomParser eventParser = new com.dianping.cat.consumer.event.model.transform.DefaultDomParser();
	
	private void showSummarizeReport(Model model, Payload payload) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIpAddress();
		if (ip == null) {
			ip = CatString.ALL_IP;
		}
		model.setIpAddress(ip);
		
		EventReport eventReport = null;
		try {
			Date start = payload.getHistoryStartDate();
			Date end = payload.getHistoryEndDate();
			String domain = model.getDomain();
			List<Dailyreport> reports = dailyreportDao.findAllByDomainNameDuration(start, end, domain, "event", DailyreportEntity.READSET_FULL);
			EventReportMerger merger = new EventReportMerger(new EventReport(domain));
			for (Dailyreport report : reports) {
				String xml = report.getContent();
				EventReport reportModel = eventParser.parse(xml);
				reportModel.accept(merger);
			}
			eventReport = merger == null ? null : merger.getEventReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (eventReport == null) {
			return;
		}
		model.setReport(eventReport);
		if (!StringUtils.isEmpty(type)) {
			model.setDisplayNameReport(new DisplayEventNameReport().display(sorted, type, ip, eventReport));
		} else {
			model.setDisplayTypeReport(new DisplayEventTypeReport().display(sorted, ip, eventReport));
		}	   
   }

	public void normalize(Model model, Payload payload) {
	   if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		String ip = payload.getIpAddress();
		if (StringUtils.isEmpty(ip)) {
			payload.setIpAddress(CatString.ALL_IP);
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
		if(payload.getAction()==Action.HISTORY_REPORT||payload.getAction()==Action.HISTORY_GRAPH){
			String type = payload.getReportType();
			if(type==null||type.length()==0){
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());
		}
   }

	private MobileEventGraphs showMobileGraphs(Model model, Payload payload) {
		EventName name = getEventName(payload);

		if (name == null) {
			return null;
		}
		MobileEventGraphs graphs = new MobileEventGraphs().display(name);
		return graphs;
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
				model.setDisplayNameReport(new DisplayEventNameReport().display(sorted, type, ip, report));
			} else {
				model.setDisplayTypeReport(new DisplayEventTypeReport().display(sorted, ip, report));
			}
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
			model.setException(e);
		}
	}
}
