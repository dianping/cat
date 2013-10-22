package com.dianping.cat.report.page.cache;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.event.EventMergeManager;
import com.dianping.cat.report.page.event.TpsStatistics;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.transaction.MergeAllMachine;
import com.dianping.cat.report.page.transaction.MergeAllName;
import com.dianping.cat.report.page.transaction.TransactionMergeManager;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class Handler implements PageHandler<Context> {

	@Inject(type = ModelService.class, value = EventAnalyzer.ID)
	private ModelService<EventReport> m_eventService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private TransactionMergeManager m_transactionMergeManger;

	@Inject
	private EventMergeManager m_eventMergerMergeManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_transactionService;

	private Set<String> m_cacheTypes;

	private CacheReport buildCacheReport(TransactionReport transactionReport, EventReport eventReport, String type,
	      String sortBy, String queryName, String ip) {
		TransactionReportVistor vistor = new TransactionReportVistor();

		vistor.setType(type).setQueryName(queryName).setSortBy(sortBy).setCurrentIp(ip);
		vistor.setEventReport(eventReport);
		vistor.visitTransactionReport(transactionReport);
		return vistor.getCacheReport();
	}

	private void calculateEventTps(Payload payload, EventReport report) {
		try {
			if (report != null) {
				boolean isCurrent = payload.getPeriod().isCurrent();
				double seconds = 0;

				if (isCurrent) {
					seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / 1000.0;
				} else {
					seconds = (report.getEndTime().getTime() - report.getStartTime().getTime()) / 1000.0;
				}
				new TpsStatistics(seconds).visitEventReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void calculateTransactionTps(Payload payload, TransactionReport report) {
		try {
			if (report != null) {
				boolean isCurrent = payload.getPeriod().isCurrent();
				double seconds = 0;

				if (isCurrent) {
					seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / 1000.0;
				} else {
					seconds = (report.getEndTime().getTime() - report.getStartTime().getTime()) / 1000.0;
				}
				new com.dianping.cat.report.page.transaction.TpsStatistics(seconds).visitTransactionReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private EventReport getHistoryEventReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		EventReport eventReport = m_reportService.queryEventReport(domain, start, end);
		return eventReport;
	}

	private TransactionReport getHistoryTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		
		return m_reportService.queryTransactionReport(domain, start, end);
	}

	private EventReport getHourlyEventReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();

		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("ip", ipAddress);

		if (StringUtils.isEmpty(type)) {
			EventReportMerger merger = new EventReportMerger(new EventReport(domain));

			for (String cacheType : m_cacheTypes) {
				request.setProperty("type", cacheType);
				ModelResponse<EventReport> response = m_eventService.invoke(request);
				EventReport eventReport = response.getModel();

				merger.visitEventReport(eventReport);
			}
			EventReport eventReport = merger.getEventReport();

			eventReport = m_eventMergerMergeManager.mergerAllIp(eventReport, ipAddress);
			return eventReport;

		} else {
			request.setProperty("type", type);
			ModelResponse<EventReport> response = m_eventService.invoke(request);
			EventReport eventReport = response.getModel();

			eventReport = m_eventMergerMergeManager.mergerAllIp(eventReport, ipAddress);
			return eventReport;
		}
	}

	private TransactionReport getHourlyTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();

		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("ip", ipAddress);

		ModelResponse<TransactionReport> all = m_transactionService.invoke(request);
		TransactionReport report = all.getModel();
		ReportVisitor visitor = new ReportVisitor();
		visitor.visitTransactionReport(report);

		m_cacheTypes = visitor.getCacheTypes();
		if (StringUtils.isEmpty(type)) {
			TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

			for (String temp : m_cacheTypes) {
				request.setProperty("type", temp);
				ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
				TransactionReport transactionReport = response.getModel();

				merger.visitTransactionReport(transactionReport);
			}

			TransactionReport transactionReport = merger.getTransactionReport();
			transactionReport = m_transactionMergeManger.mergerAllIp(transactionReport, ipAddress);
			return transactionReport;
		} else {
			request.setProperty("type", type);
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			TransactionReport transactionReport = response.getModel();

			transactionReport = m_transactionMergeManger.mergerAllIp(transactionReport, ipAddress);
			return transactionReport;
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cache")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cache")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();

		normalize(model, payload);
		switch (payload.getAction()) {
		case HOURLY_REPORT:
			TransactionReport transactionReport = getHourlyTransactionReport(payload);
			EventReport eventReport = getHourlyEventReport(payload);

			if (payload.getPeriod().isLast()) {
				Set<String> domains = m_reportService.queryAllDomainNames(new Date(payload.getDate()),
				      new Date(payload.getDate() + TimeUtil.ONE_HOUR), TransactionAnalyzer.ID);
				Set<String> domainNames = transactionReport.getDomainNames();

				domainNames.addAll(domains);
			}
			if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
				MergeAllMachine all = new MergeAllMachine();

				all.visitTransactionReport(transactionReport);
				transactionReport = all.getReport();

				com.dianping.cat.report.page.event.MergeAllMachine allEvent = new com.dianping.cat.report.page.event.MergeAllMachine();

				allEvent.visitEventReport(eventReport);
				eventReport = allEvent.getReport();
			}

			if (Constants.ALL.equalsIgnoreCase(type)) {
				MergeAllName all = new MergeAllName();

				all.visitTransactionReport(transactionReport);
				transactionReport = all.getReport();

				com.dianping.cat.report.page.event.MergeAllName allEvent = new com.dianping.cat.report.page.event.MergeAllName();

				allEvent.visitEventReport(eventReport);
				eventReport = allEvent.getReport();
			}

			calculateEventTps(payload, eventReport);
			calculateTransactionTps(payload, transactionReport);
			CacheReport cacheReport = buildCacheReport(transactionReport, eventReport, payload.getType(),
			      payload.getSortBy(), payload.getQueryName(), payload.getIpAddress());

			model.setReport(cacheReport);
			break;
		case HISTORY_REPORT:
			TransactionReport transactionHistoryReport = getHistoryTransactionReport(payload);
			EventReport eventHistoryReport = getHistoryEventReport(payload);

			calculateEventTps(payload, eventHistoryReport);
			calculateTransactionTps(payload, transactionHistoryReport);
			CacheReport cacheHistoryReport = buildCacheReport(transactionHistoryReport, eventHistoryReport,
			      payload.getType(), payload.getSortBy(), payload.getQueryName(), payload.getIpAddress());

			model.setReport(cacheHistoryReport);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		m_normalizePayload.normalize(model, payload);
		model.setPage(ReportPage.CACHE);
		model.setQueryName(payload.getQueryName());
	}

	public static class ReportVisitor extends BaseVisitor {

		private Set<String> m_cacheTypes = new HashSet<String>();

		@Override
		public void visitType(TransactionType type) {
			String typeName = type.getId();

			if (typeName.startsWith("Cache.")) {
				m_cacheTypes.add(typeName);
			}
		}

		public Set<String> getCacheTypes() {
			return m_cacheTypes;
		}
	}

}
