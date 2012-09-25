package com.dianping.cat.report.page.cache;

import java.io.IOException;
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
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.event.EventMerger;
import com.dianping.cat.report.task.transaction.TransactionMerger;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private EventMerger m_eventMerger;

	@Inject(type = ModelService.class, value = "event")
	private ModelService<EventReport> m_eventService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	private TransactionMerger m_transactionMerger;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_transactionService;

	private CacheReport buildCacheReport(TransactionReport transactionReport, EventReport eventReport, String type,
	      String sortBy, String queryName, String ip) {
		TransactionReportVistor vistor = new TransactionReportVistor();
		vistor.setType(type).setQueryName(queryName).setSortBy(sortBy).setCurrentIp(ip);
		vistor.setEventReport(eventReport);
		vistor.visitTransactionReport(transactionReport);
		return vistor.getCacheReport();
	}

	private void calculateEventTps(Payload payload, EventReport report) {
		if (payload != null && report != null) {
			boolean isCurrent = payload.getPeriod().isCurrent();
			String ip = payload.getIpAddress();
			com.dianping.cat.consumer.event.model.entity.Machine machine = report.getMachines().get(ip);
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
				}
			}
		}
	}

	private void calculateTransactionTps(Payload payload, TransactionReport report) {
		if (payload != null && report != null) {
			boolean isCurrent = payload.getPeriod().isCurrent();
			String ip = payload.getIpAddress();
			Machine machine = report.getMachines().get(ip);
			if (machine == null) {
				return;
			}
			for (TransactionType transType : machine.getTypes().values()) {
				long totalCount = transType.getTotalCount();
				double tps = 0;
				if (isCurrent) {
					double seconds = (System.currentTimeMillis() - payload.getCurrentDate()) / (double) 1000;
					tps = totalCount / seconds;
				} else {
					double time = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
					tps = totalCount / (double) time;
				}
				transType.setTps(tps);
				for (TransactionName transName : transType.getNames().values()) {
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
				}
			}
		}
	}

	private EventReport getHistoryEventReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		EventReport eventReport = null;
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
					EventReport reportModel = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				eventReport = merger.getEventReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		if (eventReport != null) {
			eventReport.setStartTime(start);
			eventReport.setEndTime(end);
		}
		return eventReport;
	}

	private TransactionReport getHistoryTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Date currentDayStart = TaskHelper.todayZero(new Date());

		TransactionReport transactionReport = null;
		if (currentDayStart.getTime() == start.getTime()) {
			try {
				List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
				      ReportEntity.READSET_FULL);
				List<Report> allReports = m_reportDao.findAllByDomainNameDuration(start, end, null, "transaction",
				      ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domains = new HashSet<String>();
				for (Report report : allReports) {
					domains.add(report.getDomain());
				}
				transactionReport = m_transactionMerger.mergeForDaily(domain, reports, domains);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} else {
			try {
				List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
				      DailyreportEntity.READSET_FULL);
				TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
				for (Dailyreport report : reports) {
					String xml = report.getContent();
					TransactionReport reportModel = DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				}
				transactionReport = merger.getTransactionReport();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		if (transactionReport != null) {
			transactionReport.setStartTime(start);
			transactionReport.setEndTime(end);
		}
		return transactionReport;
	}

	private EventReport getHourlyEventReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();

		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", ipAddress);

		if (StringUtils.isEmpty(type)) {
			request.setProperty("type", "Cache.web");
			ModelResponse<EventReport> response = m_eventService.invoke(request);
			EventReport webCacheReport = response.getModel();

			request.setProperty("type", "Cache.memcached");
			ModelResponse<EventReport> memcachedResponse = m_eventService.invoke(request);
			EventReport memcachedReport = memcachedResponse.getModel();

			request.setProperty("type", "Cache.kvdb");
			ModelResponse<EventReport> kvdbResponse = m_eventService.invoke(request);
			EventReport kvdbReport = kvdbResponse.getModel();

			request.setProperty("type", "Cache.memcached-tuangou");
			ModelResponse<EventReport> tuangouResponse = m_eventService.invoke(request);
			EventReport tuangouReport = tuangouResponse.getModel();

			EventReportMerger merger = new EventReportMerger(new EventReport(domain));

			merger.visitEventReport(webCacheReport);
			merger.visitEventReport(memcachedReport);
			merger.visitEventReport(kvdbReport);
			merger.visitEventReport(tuangouReport);
			return merger.getEventReport();

		} else {
			request.setProperty("type", type);
			ModelResponse<EventReport> response = m_eventService.invoke(request);
			return response.getModel();
		}
	}

	private TransactionReport getHourlyTransactionReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();

		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", ipAddress);

		if (StringUtils.isEmpty(type)) {
			request.setProperty("type", "Cache.web");
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			TransactionReport webCacheReport = response.getModel();

			request.setProperty("type", "Cache.memcached");
			ModelResponse<TransactionReport> memcachedResponse = m_transactionService.invoke(request);
			TransactionReport memcachedReport = memcachedResponse.getModel();

			request.setProperty("type", "Cache.kvdb");
			ModelResponse<TransactionReport> kvdbResponse = m_transactionService.invoke(request);
			TransactionReport kvdbReport = kvdbResponse.getModel();

			request.setProperty("type", "Cache.memcached-tuangou");
			ModelResponse<TransactionReport> tuangouResponse = m_transactionService.invoke(request);
			TransactionReport tuangouReport = tuangouResponse.getModel();

			TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

			merger.visitTransactionReport(webCacheReport);
			merger.visitTransactionReport(memcachedReport);
			merger.visitTransactionReport(kvdbReport);
			merger.visitTransactionReport(tuangouReport);
			return merger.getTransactionReport();
		} else {
			request.setProperty("type", type);
			ModelResponse<TransactionReport> response = m_transactionService.invoke(request);
			return response.getModel();
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

		normalize(model, payload);
		switch (payload.getAction()) {
		case HOURLY_REPORT:
			TransactionReport transactionReport = getHourlyTransactionReport(payload);
			EventReport eventReport = getHourlyEventReport(payload);

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
		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		String ip = payload.getIpAddress();
		if (StringUtils.isEmpty(ip)) {
			payload.setIpAddress(CatString.ALL_IP);
		}
		model.setIpAddress(payload.getIpAddress());
		model.setAction(payload.getAction());
		model.setPage(ReportPage.CACHE);
		model.setDisplayDomain(payload.getDomain());
		model.setQueryName(payload.getQueryName());
		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
		if (payload.getAction() == Action.HISTORY_REPORT) {
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
}
