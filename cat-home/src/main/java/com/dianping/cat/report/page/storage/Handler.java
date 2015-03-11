package com.dianping.cat.report.page.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoManager;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.StorageGroupConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = StorageAnalyzer.ID)
	private ModelService<StorageReport> m_service;

	@Inject
	private StorageMergeHelper m_mergeHelper;

	@Inject
	private StorageAlertInfoManager m_alertInfoManager;

	@Inject
	private StorageGroupConfigManager m_storageGroupConfigManager;

	@Inject
	private JsonBuilder m_jsonBuilder;

	private void buildLineCharts(Model model, Payload payload, String ipAddress, StorageReport storageReport) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(ipAddress, payload.getProject(),
		      model.getOperations(), storageReport.getStartTime());

		visitor.visitStorageReport(storageReport);
		Map<String, LineChart> lineCharts = visitor.getLineChart();

		model.setCountTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.COUNT)));
		model.setAvgTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.AVG)));
		model.setErrorTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.ERROR)));
		model.setLongTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.LONG)));
	}

	private void buildOperations(Payload payload, Model model) {
		String operations = payload.getOperations();

		if (StringUtils.isNotEmpty(operations)) {
			String[] op = operations.split(";");
			Set<String> ops = new HashSet<String>();

			for (int i = 0; i < op.length; i++) {
				ops.add(op[i]);
			}
			model.setOperations(ops);
		}
	}

	private StorageReport buildReport(Payload payload, Model model, StorageReport storageReport) {
		if (storageReport != null) {
			storageReport = m_mergeHelper.mergeReport(storageReport, payload.getIpAddress(), Constants.ALL);
			StorageSorter sorter = new StorageSorter(storageReport, payload.getSort());
			storageReport = sorter.getSortedReport();

			model.setReport(storageReport);
			model.setOperations(storageReport.getOps());
		}
		buildOperations(payload, model);
		return storageReport;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "storage")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "storage")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);
		String ipAddress = payload.getIpAddress();
		StorageReport storageReport = null;

		switch (payload.getAction()) {
		case HOURLY_DATABASE:
			storageReport = queryHourlyReport(payload, StorageConstants.SQL_TYPE);

			buildReport(payload, model, storageReport);
			break;
		case HOURLY_CACHE:
			storageReport = queryHourlyReport(payload, StorageConstants.CACHE_TYPE);

			buildReport(payload, model, storageReport);
			break;
		case HOURLY_DATABASE_GRAPH:
			storageReport = queryHourlyReport(payload, StorageConstants.SQL_TYPE);

			storageReport = buildReport(payload, model, storageReport);
			buildLineCharts(model, payload, ipAddress, storageReport);
			break;
		case HOURLY_CACHE_GRAPH:
			storageReport = queryHourlyReport(payload, StorageConstants.CACHE_TYPE);

			buildReport(payload, model, storageReport);
			buildLineCharts(model, payload, ipAddress, storageReport);
			break;
		case HISTORY_DATABASE:
			storageReport = queryHistoryReport(payload, StorageConstants.SQL_TYPE);

			buildReport(payload, model, storageReport);
			break;
		case HISTORY_CACHE:
			storageReport = queryHistoryReport(payload, StorageConstants.CACHE_TYPE);

			buildReport(payload, model, storageReport);
			break;
		case DASHBOARD:
			Map<String, StorageAlertInfo> alertInfo = m_alertInfoManager.queryAlertInfos(payload, model);

			model.setAlertInfos(alertInfo);
			model.setReportStart(new Date(payload.getDate()));
			model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));
			break;
		}
		model.setPage(ReportPage.STORAGE);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STORAGE);
		m_normalizePayload.normalize(model, payload);

		if (payload.getAction() == Action.DASHBOARD) {
			Integer minute = parseQueryMinute(payload);
			int maxMinute = 60;
			List<Integer> minutes = new ArrayList<Integer>();

			if (payload.getPeriod().isCurrent()) {
				long current = payload.getCurrentTimeMillis() / 1000 / 60;
				maxMinute = (int) (current % (60));
			}
			for (int i = 0; i < 60; i++) {
				minutes.add(i);
			}
			model.setMinute(minute);
			model.setMaxMinute(maxMinute);
			model.setMinutes(minutes);
		}
	}

	private int parseQueryMinute(Payload payload) {
		int minute = 0;
		String min = payload.getMinute();

		if (StringUtils.isEmpty(min)) {
			long current = payload.getCurrentTimeMillis() / 1000 / 60;
			minute = (int) (current % (60));
		} else {
			minute = Integer.parseInt(min);
		}

		return minute;
	}

	public StorageReport queryHistoryReport(Payload payload, String type) {
		String id = payload.getId();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryStorageReport(id + "-" + type, start, end);
	}

	private StorageReport queryHourlyReport(Payload payload, String type) {
		ModelRequest request = new ModelRequest(payload.getId() + "-" + type, payload.getDate()).setProperty("ip",
		      payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StorageReport> response = m_service.invoke(request);
			StorageReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}
}
