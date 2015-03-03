package com.dianping.cat.report.page.storage;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
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
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

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
	private JsonBuilder m_jsonBuilder;

	private static final String SQL_TYPE = "SQL";

	private static final String CACHE_TYPE = "Cache";

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
		StorageReport storageReport;

		switch (payload.getAction()) {
		case HOURLY_DATABASE:
			storageReport = buildHourlyReport(payload, model, ipAddress, SQL_TYPE);
			StorageSorter sorter = new StorageSorter(storageReport, payload.getSort());
			storageReport = sorter.getSortedReport();
			break;
		case HOURLY_CACHE:
			storageReport = buildHourlyReport(payload, model, ipAddress, CACHE_TYPE);
			sorter = new StorageSorter(storageReport, payload.getSort());
			storageReport = sorter.getSortedReport();
			break;
		case HOURLY_DATABASE_GRAPH:
			storageReport = buildHourlyReport(payload, model, ipAddress, SQL_TYPE);

			buildLineCharts(model, payload, ipAddress, storageReport);
			break;
		case HOURLY_CACHE_GRAPH:
			storageReport = buildHourlyReport(payload, model, ipAddress, CACHE_TYPE);

			buildLineCharts(model, payload, ipAddress, storageReport);
			break;
		case HISTORY_DATABASE:
			storageReport = queryHistoryReport(payload, SQL_TYPE);
			break;
		case HISTORY_CACHE:
			storageReport = queryHistoryReport(payload, CACHE_TYPE);
			break;
		}
		model.setPage(ReportPage.STORAGE);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void buildLineCharts(Model model, Payload payload, String ipAddress, StorageReport storageReport) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(ipAddress, payload.getProject(),
		      model.getOperations(), storageReport.getStartTime());

		visitor.visitStorageReport(storageReport);
		Map<String, LineChart> lineCharts = visitor.getLineChart();

		model.setCountTrend(m_jsonBuilder.toJson(lineCharts.get("count")));
		model.setAvgTrend(m_jsonBuilder.toJson(lineCharts.get("avg")));
		model.setErrorTrend(m_jsonBuilder.toJson(lineCharts.get("error")));
		model.setLongTrend(m_jsonBuilder.toJson(lineCharts.get("long")));
	}

	private StorageReport buildHourlyReport(Payload payload, Model model, String ipAddress, String type) {
		StorageReport storageReport = queryHourlyReport(payload, type);

		if (storageReport != null) {
			storageReport = m_mergeHelper.mergeAllMachines(storageReport, ipAddress);
			storageReport = m_mergeHelper.mergeAllDomains(storageReport, Constants.ALL);

			model.setReport(storageReport);
			model.setOperations(storageReport.getOps());
		}
		String operations = payload.getOperations();

		if (StringUtils.isNotEmpty(operations)) {
			String[] op = operations.split(";");
			Set<String> ops = new HashSet<String>();

			for (int i = 0; i < op.length; i++) {
				ops.add(op[i]);
			}
			model.setOperations(ops);
		}

		return storageReport;
	}

	private StorageReport queryHourlyReport(Payload payload, String type) {
		ModelRequest request = new ModelRequest(payload.getDomain() + "-" + type, payload.getDate()).setProperty("ip",
		      payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StorageReport> response = m_service.invoke(request);
			StorageReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable transaction service registered for " + request + "!");
		}
	}

	public StorageReport queryHistoryReport(Payload payload, String type) {
		String id = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryStorageReport(id + "-" + type, start, end);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STORAGE);
		m_normalizePayload.normalize(model, payload);
	}
}
