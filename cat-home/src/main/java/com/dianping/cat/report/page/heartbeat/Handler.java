package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.view.StringSortHelper;

public class Handler implements PageHandler<Context> {
	@Inject
	private GraphBuilder m_builder;

	@Inject
	private HistoryGraphs m_historyGraphs;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject(type = ModelService.class, value = "heartbeat")
	private ModelService<HeartbeatReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

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

	private void buildHistoryGraph(Model model, Payload payload) {
		HeartbeatReport report = m_reportService.queryHeartbeatReport(payload.getDomain(), new Date(payload.getDate()),
		      new Date(payload.getDate() + TimeUtil.ONE_HOUR));
		model.setReport(report);

		if (StringUtil.isEmpty(payload.getIpAddress()) || CatString.ALL.equals(payload.getIpAddress())) {
			String ipAddress = getIpAddress(report, payload);

			payload.setIpAddress(ipAddress);
			payload.setRealIp(ipAddress);
		}
		m_historyGraphs.showHeartBeatGraph(model, payload);
	}

	private String getIpAddress(HeartbeatReport report, Payload payload) {
		Set<String> ips = report.getIps();
		String ip = payload.getRealIp();

		if ((ip == null || ip.length() == 0) && !ips.isEmpty()) {
			ip = StringSortHelper.sort(ips).get(0);
		}
		return ip;
	}

	private HeartbeatReport getReport(String domain, String ipAddress, long dateLong, ModelPeriod period) {
		String date = String.valueOf(dateLong);
		ModelRequest request = new ModelRequest(domain, period) //
		      .setProperty("date", date).setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			if (period.isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(dateLong), new Date(dateLong
				      + TimeUtil.ONE_HOUR), "heartbeat");
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
		case HISTORY:
			buildHistoryGraph(model, payload);
			break;
		case PART_HISTORY:
			buildHistoryGraph(model, payload);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		String ipAddress = payload.getIpAddress();

		model.setPage(ReportPage.HEARTBEAT);
		if (StringUtil.isEmpty(ipAddress) || ipAddress.equals(CatString.ALL)) {
			model.setIpAddress(CatString.ALL);
		} else {
			payload.setRealIp(payload.getIpAddress());
			model.setIpAddress(payload.getRealIp());
		}
		m_normalizePayload.normalize(model, payload);

		String queryType = payload.getType();

		if (queryType == null || queryType.trim().length() == 0) {
			payload.setType("frameworkThread");
		}
	}

	private DisplayHeartbeat showReport(Model model, Payload payload) {
		try {
			HeartbeatReport report = getReport(payload.getDomain(), payload.getIpAddress(), payload.getDate(),
			      payload.getPeriod());
			model.setReport(report);
			if (report != null) {
				String displayIp = getIpAddress(report, payload);

				payload.setRealIp(displayIp);
				// model.setIpAddress(ip);
				return new DisplayHeartbeat(m_builder).display(report, displayIp);
			}
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
