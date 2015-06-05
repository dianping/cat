package com.dianping.cat.report.page.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager.Department;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.storage.topology.StorageAlertInfoManager;
import com.dianping.cat.report.page.storage.transform.HourlyLineChartVisitor;
import com.dianping.cat.report.page.storage.transform.PieChartVisitor;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.storage.transform.StorageOperationFilter;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private StorageReportService m_reportService;

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

	@Inject
	private AlterationDao m_alterationDao;

	private Map<String, Map<String, List<String>>> buildAlertLinks(Map<String, StorageAlertInfo> alertInfos, String type) {
		Map<String, Map<String, List<String>>> links = new LinkedHashMap<String, Map<String, List<String>>>();
		String format = m_storageGroupConfigManager.queryLinkFormat(type);

		if (format != null) {
			for (Entry<String, StorageAlertInfo> alertInfo : alertInfos.entrySet()) {
				String key = alertInfo.getKey();
				Map<String, List<String>> linkMap = links.get(key);

				if (linkMap == null) {
					linkMap = new LinkedHashMap<String, List<String>>();
					links.put(key, linkMap);
				}
				for (Entry<String, Storage> entry : alertInfo.getValue().getStorages().entrySet()) {
					String id = entry.getKey();
					Storage storage = entry.getValue();
					List<String> ls = linkMap.get(id);

					if (ls == null) {
						ls = new ArrayList<String>();
						linkMap.put(id, ls);
					}
					for (String ip : storage.getMachines().keySet()) {
						String url = m_storageGroupConfigManager.buildUrl(format, id, ip);

						if (url != null) {
							ls.add(url);
						}
					}
				}
			}
		}
		return links;
	}

	private List<Alteration> buildAlterations(Payload payload, Model model) {
		int minuteCounts = payload.getMinuteCounts();
		int minute = model.getMinute();
		long end = payload.getDate() + (minute + 1) * TimeHelper.ONE_MINUTE - TimeHelper.ONE_SECOND;
		long start = payload.getDate() + (minute + 1 - minuteCounts) * TimeHelper.ONE_MINUTE;
		List<Alteration> results = new LinkedList<Alteration>();

		try {
			List<Alteration> alterations = m_alterationDao.findByTypeDruation(new Date(start), new Date(end),
			      payload.getType(), AlterationEntity.READSET_FULL);

			for (Alteration alteration : alterations) {
				results.add(alteration);
			}
		} catch (DalNotFoundException e) {
			// ignore it
		} catch (Exception e) {
			Cat.logError(e);
		}
		return results;
	}

	private void buildLineCharts(Model model, Payload payload, StorageReport storageReport) {
		HourlyLineChartVisitor visitor = new HourlyLineChartVisitor(payload.getIpAddress(), payload.getProject(),
		      storageReport.getOps(), storageReport.getStartTime());

		visitor.visitStorageReport(storageReport);
		Map<String, LineChart> lineCharts = visitor.getLineChart();

		model.setCountTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.COUNT)));
		model.setAvgTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.AVG)));
		model.setErrorTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.ERROR)));
		model.setLongTrend(m_jsonBuilder.toJson(lineCharts.get(StorageConstants.LONG)));
	}

	private Pair<Boolean, Set<String>> buildOperations(Payload payload, Model model, Set<String> defaultValue) {
		String operations = payload.getOperations();
		Set<String> ops = new HashSet<String>();
		boolean filter = false;

		if (operations.length() > 0) {
			filter = true;
			String[] op = operations.split(";");

			for (int i = 0; i < op.length; i++) {
				ops.add(op[i]);
			}
		} else {
			ops.addAll(defaultValue);
		}
		return new Pair<Boolean, Set<String>>(filter, ops);
	}

	private String buildOperationStr(List<String> ops) {
		return StringUtils.join(ops, ";");
	}

	private StorageReport filterReport(Payload payload, Model model, StorageReport storageReport) {
		if (storageReport != null) {
			Set<String> allOps = storageReport.getOps();
			model.setOperations(allOps);

			Pair<Boolean, Set<String>> pair = buildOperations(payload, model, allOps);
			if (pair.getKey()) {
				StorageOperationFilter filter = new StorageOperationFilter(pair.getValue());
				filter.visitStorageReport(storageReport);

				storageReport = filter.getStorageReport();
			}
		}
		return storageReport;
	}

	private StorageReport mergeReport(Payload payload, StorageReport storageReport) {
		storageReport = m_mergeHelper.mergeReport(storageReport, payload.getIpAddress(), Constants.ALL);
		StorageSorter sorter = new StorageSorter(storageReport, payload.getSort());

		return sorter.getSortedReport();
	}

	private void buildDepartments(Payload payload, Model model, StorageReport storageReport) {
		Map<String, Department> departments = m_storageGroupConfigManager.queryStorageDepartments(
		      SortHelper.sortDomain(storageReport.getIds()), payload.getType());
		model.setDepartments(departments);
	}

	private String buildReportId(Payload payload) {
		return payload.getId() + "-" + payload.getType();
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
		StorageReport storageReport = null;
		StorageReport rawReport = null;

		switch (payload.getAction()) {
		case HOURLY_STORAGE:
			storageReport = queryHourlyReport(payload);
			model.setOriginalReport(storageReport);

			rawReport = filterReport(payload, model, storageReport);
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case HOURLY_STORAGE_GRAPH:
			storageReport = queryHourlyReport(payload);
			rawReport = filterReport(payload, model, storageReport);

			if (Constants.ALL.equals(payload.getIpAddress())) {
				buildPieCharts(model, payload, rawReport);
			}
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildLineCharts(model, payload, storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case HISTORY_STORAGE:
			storageReport = queryHistoryReport(payload);
			model.setOriginalReport(storageReport);

			rawReport = filterReport(payload, model, storageReport);
			storageReport = mergeReport(payload, rawReport);

			model.setReport(storageReport);
			buildDepartments(payload, model, storageReport);
			break;
		case DASHBOARD:
			Map<String, StorageAlertInfo> alertInfos = m_alertInfoManager.queryAlertInfos(payload, model);

			model.setLinks(buildAlertLinks(alertInfos, payload.getType()));
			model.setAlertInfos(alertInfos);
			model.setReportStart(new Date(payload.getDate()));
			model.setReportEnd(new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1));
			model.setAlterations(buildAlterations(payload, model));
			break;
		}

		model.setPage(ReportPage.STORAGE);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void buildPieCharts(Model model, Payload payload, StorageReport report) {
		PieChartVisitor visitor = new PieChartVisitor();

		visitor.visitStorageReport(report);
		model.setDistributionChart(visitor.getPiechartJson());
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STORAGE);
		model.setAction(payload.getAction());
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
		} else {
			if (payload.getOperations() == null) {
				String type = payload.getType();
				List<String> defaultMethods = new ArrayList<String>();

				if (StorageConstants.CACHE_TYPE.equals(type)) {
					defaultMethods = StorageConstants.CACHE_METHODS;
				} else if (StorageConstants.SQL_TYPE.equals(type)) {
					defaultMethods = StorageConstants.SQL_METHODS;
				}

				payload.setOperations(buildOperationStr(defaultMethods));
			}
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

	public StorageReport queryHistoryReport(Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryReport(buildReportId(payload), start, end);
	}

	private StorageReport queryHourlyReport(Payload payload) {
		ModelRequest request = new ModelRequest(buildReportId(payload), payload.getDate()).setProperty("ip",
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
