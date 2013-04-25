package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.home.dal.report.GraphDao;
import com.dianping.cat.home.dal.report.GraphEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.view.StringSortHelper;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {
	@Inject
	private GraphBuilder m_builder;

	@Inject
	private GraphDao m_graphDao;

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private ReportService m_reportService;

	@Inject(type = ModelService.class, value = "heartbeat")
	private ModelService<HeartbeatReport> m_service;

	private void buildHeartbeatGraphInfo(Model model, DisplayHeartbeat displayHeartbeat) {
		if (displayHeartbeat == null) {
			return;
		}
		model.setResult(displayHeartbeat);
		model.setActiveThreadGraph(displayHeartbeat.getActiceThreadGraph());
		model.setDaemonThreadGraph(displayHeartbeat.getDeamonThreadGraph());
		model.setTotalThreadGraph(displayHeartbeat.getTotalThreadGraph());
		model.setHttpThreadGraph(displayHeartbeat.getHttpTheadGraph());
		model.setStartedThreadGraph(displayHeartbeat.getStartedThreadGraph());
		model.setCatThreadGraph(displayHeartbeat.getCatThreadGraph());
		model.setPigeonThreadGraph(displayHeartbeat.getPigeonTheadGraph());
		model.setCatMessageProducedGraph(displayHeartbeat.getCatMessageProducedGraph());
		model.setCatMessageOverflowGraph(displayHeartbeat.getCatMessageOverflowGraph());
		model.setCatMessageSizeGraph(displayHeartbeat.getCatMessageSizeGraph());
		model.setNewGcCountGraph(displayHeartbeat.getNewGcCountGraph());
		model.setOldGcCountGraph(displayHeartbeat.getOldGcCountGraph());
		model.setHeapUsageGraph(displayHeartbeat.getHeapUsageGraph());
		model.setNoneHeapUsageGraph(displayHeartbeat.getNoneHeapUsageGraph());
		model.setDisks(displayHeartbeat.getDisks());
		model.setDisksGraph(displayHeartbeat.getDisksGraph());
		model.setSystemLoadAverageGraph(displayHeartbeat.getSystemLoadAverageGraph());
		model.setMemoryFreeGraph(displayHeartbeat.getMemoryFreeGraph());
	}

	private String getIpAddress(HeartbeatReport report, Payload payload) {
		Set<String> ips = report.getIps();
		String ip = payload.getIpAddress();

		if ((ip == null || ip.length() == 0) && !ips.isEmpty()) {
			ip = StringSortHelper.sort(ips).get(0);
		}

		return ip;
	}

	private HeartbeatReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date).setProperty("ip", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), "heartbeat");
				Set<String> domainNames = report.getDomainNames();

				domainNames.addAll(domains);
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "h")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "h")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		DisplayHeartbeat heartbeat = null;

		normalize(model, payload);
		switch (payload.getAction()) {
		case VIEW:
			heartbeat = showReport(model, payload);
			buildHeartbeatGraphInfo(model, heartbeat);
			break;
		case MOBILE:
			heartbeat = showReport(model, payload);
			MobileHeartbeat mobileModel = setMobileModel(model, heartbeat);
			String json = new Gson().toJson(mobileModel);

			model.setMobileResponse(json);
			break;
		case HISTORY:
			if (model.getIpAddress() != null) {
				m_historyGraphs.showHeartBeatGraph(model, payload);
			}
			break;
		case PART_HISTORY:
			if (model.getIpAddress() != null) {
				m_historyGraphs.showHeartBeatGraph(model, payload);
			}
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		model.setAction(payload.getAction());
		model.setPage(ReportPage.HEARTBEAT);
		model.setIpAddress(payload.getIpAddress());
		String queryType = payload.getType();

		if (queryType == null || queryType.trim().length() == 0) {
			payload.setType("frameworkThread");
		}
		Action action = payload.getAction();
		if (action == Action.HISTORY) {
			String type = payload.getReportType();

			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			model.setLongDate(payload.getDate());

			HeartbeatReport report = new HeartbeatReport();

			model.setReport(report);
			try {
				Date historyStartDate = payload.getHistoryStartDate();
				Date historyEndDate = payload.getHistoryEndDate();
				List<Graph> domains = m_graphDao.findDomainByNameDuration(historyStartDate, historyEndDate, "heartbeat",
				      GraphEntity.READSET_DOMAIN);
				String domain = payload.getDomain();
				List<Graph> ips = m_graphDao.findIpByDomainNameDuration(historyStartDate, historyEndDate, domain,
				      "heartbeat", GraphEntity.READSET_IP);
				Set<String> reportDomains = report.getDomainNames();
				Set<String> reportIps = report.getIps();

				for (Graph graph : domains) {
					reportDomains.add(graph.getDomain());
				}
				for (Graph graph : ips) {
					reportIps.add(graph.getIp());
				}
				report.setDomain(payload.getDomain());
				model.setDisplayDomain(payload.getDomain());

				String ip = payload.getIpAddress();
				if (StringUtils.isEmpty(ip)) {
					List<String> ips2 = model.getIps();
					if (ips2.size() > 0) {
						ip = ips2.get(0);
					}
				}
				model.setIpAddress(ip);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	private MobileHeartbeat setMobileModel(Model model, DisplayHeartbeat heartbeat) {
		MobileHeartbeat result = new MobileHeartbeat();
		result.display(model, heartbeat);
		return result;
	}

	private DisplayHeartbeat showReport(Model model, Payload payload) {
		try {
			ModelPeriod period = payload.getPeriod();

			if (period.isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}
			model.setDisplayDomain(payload.getDomain());

			HeartbeatReport report = getReport(payload);
			if (report == null) {
				return null;
			}
			model.setReport(report);
			String ip = getIpAddress(report, payload);
			model.setIpAddress(ip);

			DisplayHeartbeat displayHeartbeat = new DisplayHeartbeat(m_builder).display(report, ip);
			return displayHeartbeat;
		} catch (Throwable e) {
			Cat.logError(e);
			model.setException(e);
		}
		return null;
	}

	// the detail order of heartbeat is:name min max sum sum2 count_in_minutes
	public enum DetailOrder {
		NAME, MIN, MAX, SUM, SUM2, COUNT_IN_MINUTES
	}

}
