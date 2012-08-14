package com.dianping.cat.notify.report;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.dianping.cat.CatConstants;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.notify.dao.DailyReportDao;
import com.dianping.cat.notify.job.ProblemStatistics;
import com.dianping.cat.notify.job.ProblemStatistics.TypeStatistics;
import com.dianping.cat.notify.model.DailyReport;
import com.dianping.cat.notify.render.IRender;
import com.dianping.cat.notify.server.ContainerHolder;
import com.dianping.cat.notify.util.TimeUtil;

public abstract class AbstractReportCreater implements ReportCreater {

	private static final String TRENDS_URL = "<a href='http://cat.dianpingoa.com/cat/r/%s?op=historyGraph&domain=%s&date=%s&ip=All&reportType=%s&type=%s' target='_blank'>%s</a>";

	private static final String CURRENT_URL = "<a href='http://cat.dianpingoa.com/cat/r/%s?domain=%s&date=%s&reportType=&op=view' target='_blank'>%s</a>";

	private final static Logger logger = LoggerFactory.getLogger(AbstractReportCreater.class);

	protected ReportConfig m_config;

	protected ContainerHolder m_configHolder;

	protected DailyReportDao m_dailyReportDao;

	protected IRender m_render;

	protected AtomicLong lastSuccessTime = new AtomicLong();

	private static Set<String> reportNames = new HashSet<String>();

	@Override
	public boolean init(ReportConfig config, ContainerHolder holder) {
		m_config = config;
		m_configHolder = holder;
		m_dailyReportDao = m_configHolder.lookup(DailyReportDao.class, "dailyReportDao");
		m_render = m_configHolder.lookup(IRender.class, "render");

		if (m_config.getSchedule() == null || m_config.getSchedule().trim().length() == 0) {
			logger.error("fail to init cronExpression,Schedule isEmpty");
			return false;
		}
		lastSuccessTime.set(-1);

		reportNames.add("transaction");
		reportNames.add("event");
		reportNames.add("problem");
		return true;
	}

	public abstract boolean isNeedToCreate(long timestamp);

	public final String createReport(long timestamp, String domain) {
		TimeSpan timeRange = getReportTimeSpan(timestamp);
		if (timeRange == null) {
			logger.error(String.format("fail to get timeRange,timestamp[%s]", timestamp));
			return null;
		}

		long startMicros = timeRange.getStartMicros();
		long endMicros = timeRange.getEndMicros();

		StringBuilder report_content = new StringBuilder();
		try {
			for (String reportName : reportNames) {
				List<DailyReport> dailyReportList = new ArrayList<DailyReport>();
				dailyReportList = m_dailyReportDao.findAllByDomainNameDuration(new Date(startMicros), new Date(endMicros),
				      domain, reportName, DailyReport.XML_TYPE);
				if (reportName.equals(DailyReport.EVENT_REPORT)) {
					EventReport eReport = parseEvent(dailyReportList, domain);
					report_content.append(renderEventReport(timeRange, eReport, domain));
				} else if (reportName.equals(DailyReport.PROBLEM_REPORT)) {
					ProblemReport pReport = parseProblem(dailyReportList, domain);
					report_content.append(renterProblemReport(timeRange, pReport, domain));
				} else if (reportName.equals(DailyReport.TRANSACGION_REPORT)) {
					TransactionReport tReport = parseTransction(dailyReportList, domain);
					caculateTps(tReport, ReportConstants.ALL_IP);
					report_content.append(renderTransactionReport(timeRange, tReport, domain));
				}
			}
		} catch (Exception e) {
			logger.error(String.format("fail to read report from database,time range[%s,%s]", new Date(startMicros),
			      new Date(endMicros)), e);
			return null;
		}

		return report_content.toString();
	}

	private EventReport parseEvent(List<DailyReport> reportList, String domain) {
		if (reportList == null || reportList.size() == 0) {
			return null;
		}
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));
		for (DailyReport dailyReport : reportList) {
			String xml = dailyReport.getContent();
			if (xml == null) {
				continue;
			}
			try {
				EventReport model = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			} catch (IOException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			}
		}
		return merger.getEventReport();
	}

	private ProblemReport parseProblem(List<DailyReport> reportList, String domain) {
		if (reportList == null || reportList.size() == 0) {
			return null;
		}
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));
		for (DailyReport dailyReport : reportList) {
			String xml = dailyReport.getContent();
			if (xml == null) {
				continue;
			}
			try {
				ProblemReport model = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			} catch (IOException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			}
		}
		return merger.getProblemReport();
	}

	private TransactionReport parseTransction(List<DailyReport> reportList, String domain) {
		if (reportList == null || reportList.size() == 0) {
			return null;
		}
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		for (DailyReport dailyReport : reportList) {
			String xml = dailyReport.getContent();
			if (xml == null) {
				continue;
			}
			try {
				TransactionReport model = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			} catch (IOException e) {
				logger.error(String.format("fail to parse the report,domain:[%s]  name[%s], period[%s]",
				      dailyReport.getDomain(), dailyReport.getName(), dailyReport.getPeriod()), e);
				continue;
			}
		}
		return merger.getTransactionReport();
	}

	// http://cat.dianpingoa.com/cat/r/%s?op=historyGraph&domain=%s&date=%s&ip=All&reportType=%s&type=%s'>%s
	protected String getTrendsViewUrl(String reportType, String domain, long timestamp, String dayOrWeak, String name,
	      String hyperText) {
		return String.format(TRENDS_URL, reportType, domain, TimeUtil.formatTime("yyyyMMdd", timestamp), dayOrWeak, name,
		      hyperText);
	}

	// http://cat.dianpingoa.com/cat/r/%s?domain=%s&date=%s&reportType=&op=view
	protected String getCurrentViewUrl(String reportType, String domain, long timestamp) {
		String reportDay = TimeUtil.formatTime("yyyy-MM-dd", timestamp);
		return String.format(CURRENT_URL, reportType, domain, TimeUtil.formatTime("yyyyMMdd", timestamp), reportDay);

	}

	private void caculateTps(TransactionReport report, String ip) {
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
			return;
		}
		for (TransactionType transType : machine.getTypes().values()) {
			long totalCount = transType.getTotalCount();
			double tps = 0;
			double time = (report.getEndTime().getTime() - report.getStartTime().getTime()) / (double) 1000;
			tps = totalCount / (double) time;
			transType.setTps(tps);
			for (TransactionName transName : transType.getNames().values()) {
				long totalNameCount = transName.getTotalCount();
				double nameTps = 0;
				nameTps = totalNameCount / (double) time;
				transName.setTps(nameTps);
			}
		}
	}

	protected abstract TimeSpan getReportTimeSpan(long timespan);

	protected abstract String renderTransactionReport(TimeSpan timeSpan, TransactionReport report, String domain);

	protected abstract String renderEventReport(TimeSpan timeSpan, EventReport report, String domain);

	protected abstract String renterProblemReport(TimeSpan timeSpan, ProblemReport report, String domain);

	protected List<TransactionRenderDO> getTransactionRenderDoList(TimeSpan timeSpan,
	      TransactionReport transactionReport, String domain, boolean isWeek) {
		String urlType = "day";
		if (isWeek) {
			urlType = "week";
		}
		com.dianping.cat.consumer.transaction.model.entity.Machine machine = transactionReport
		      .findMachine(ReportConstants.ALL_IP);
		if (machine == null) {
			return null;
		}
		Map<String, TransactionType> types = machine.getTypes();
		types.remove("Task");
		types.remove("System");
		types.remove("Result");

		List<TransactionType> typeList = new ArrayList<TransactionType>(types.values());
		if (typeList.size() == 0) {
			return null;
		}
		Collections.sort(typeList, new Comparator<TransactionType>() {
			@Override
			public int compare(TransactionType o1, TransactionType o2) {
				return (int) (o2.getAvg() - o1.getAvg());
			}
		});

		List<TransactionRenderDO> tRenderDoList = new ArrayList<TransactionRenderDO>();
		DecimalFormat floatFormat = new DecimalFormat(",###.##");
		DecimalFormat integerFormat = new DecimalFormat(",###");
		for (TransactionType transactionType : typeList) {
			String trendViewUrl = getTrendsViewUrl("t", domain, timeSpan.getEndMicros(), urlType, transactionType.getId(),
			      "Graph");
			TransactionRenderDO renderDO = new TransactionRenderDO();
			renderDO.setId(transactionType.getId());
			renderDO.setAvg(floatFormat.format(transactionType.getAvg()));
			renderDO.setFailCount(integerFormat.format(transactionType.getFailCount()));
			renderDO.setFailPercent(floatFormat.format(transactionType.getFailPercent()));
			renderDO.setTotalCount(integerFormat.format(transactionType.getTotalCount()));
			renderDO.setTps(floatFormat.format(transactionType.getTps()));
			renderDO.setLink(trendViewUrl);
			tRenderDoList.add(renderDO);
		}
		return tRenderDoList;
	}

	public List<EventRenderDO> getEventRenderDoList(TimeSpan timeSpan, EventReport report, String domain, boolean isWeek) {
		String urlType = "day";
		if (isWeek) {
			urlType = "week";
		}
		com.dianping.cat.consumer.event.model.entity.Machine machine = report.findMachine(ReportConstants.ALL_IP);
		if (machine == null) {
			return null;
		}
		Map<String, EventType> types = machine.getTypes();
		types.remove(CatConstants.TYPE_URL);
		types.remove(CatConstants.TYPE_SQL_PARAM);
		types.remove(CatConstants.TYPE_PIGEON_REQUEST);
		types.remove(CatConstants.TYPE_PIGEON_RESPONSE);
		types.remove(CatConstants.TYPE_REMOTE_CALL);

		List<EventType> eventTypeList = new ArrayList<EventType>(types.values());

		if (eventTypeList.size() == 0) {
			return null;
		}
		Collections.sort(eventTypeList, new Comparator<EventType>() {
			@Override
			public int compare(EventType o1, EventType o2) {
				return (int) (o2.getTotalCount() - o1.getTotalCount());
			}
		});
		List<EventRenderDO> eRenderDoList = new ArrayList<EventRenderDO>();
		DecimalFormat floatFormat = new DecimalFormat(",###.##");
		DecimalFormat integerFormat = new DecimalFormat(",###");
		for (EventType eventType : eventTypeList) {
			String trendViewUrl = getTrendsViewUrl("e", domain, timeSpan.getEndMicros(), urlType, eventType.getId(),"Graph");
			eventType.setSuccessMessageUrl(trendViewUrl);
			EventRenderDO eventRenderDO = new EventRenderDO();
			eventRenderDO.setFailCount(integerFormat.format(eventType.getFailCount()));
			eventRenderDO.setFailPercent(floatFormat.format(eventType.getFailPercent()));
			eventRenderDO.setId(eventType.getId());
			eventRenderDO.setLink(trendViewUrl);
			eventRenderDO.setTotalCount(integerFormat.format(eventType.getTotalCount()));
			eRenderDoList.add(eventRenderDO);
		}
		return eRenderDoList;
	}

	protected List<ProblemRenderDO> getProblemRenderDoList(TimeSpan timeSpan, ProblemReport report, String domain,
	      boolean isWeek) {
		String urlType = "day";
		if (isWeek) {
			urlType = "week";
		}
		List<ProblemRenderDO> pRenderDoList = new ArrayList<ProblemRenderDO>();
		ProblemStatistics problemStatistics = new ProblemStatistics();
		problemStatistics.setAllIp(true);
		problemStatistics.visitProblemReport(report);
		List<TypeStatistics> typeStatisticsList = new ArrayList<TypeStatistics>(problemStatistics.getStatus().values());
		Collections.sort(typeStatisticsList, new Comparator<TypeStatistics>() {
			@Override
			public int compare(TypeStatistics o1, TypeStatistics o2) {
				return (int) (o2.getCount() - o1.getCount());
			}
		});
		
		DecimalFormat integerFormat = new DecimalFormat(",###");
		for (TypeStatistics typeStatistics : typeStatisticsList) {
			String trendViewUrl = getTrendsViewUrl("p", domain, timeSpan.getEndMicros(), urlType, typeStatistics.getType(), "Graph");
			typeStatistics.setTrendUrl(trendViewUrl);
			ProblemRenderDO problemRenderDO = new ProblemRenderDO();
			problemRenderDO.setCount(integerFormat.format(typeStatistics.getCount()));
			problemRenderDO.setTrendUrl(trendViewUrl);
			problemRenderDO.setType(typeStatistics.getType());
			pRenderDoList.add(problemRenderDO);
		}
		
		return pRenderDoList;
	}

}

class TimeSpan {

	long startMicros;

	long endMicros;

	long timeStamp;

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getStartMicros() {
		return startMicros;
	}

	public void setStartMicros(long startMicros) {
		this.startMicros = startMicros;
	}

	public long getEndMicros() {
		return endMicros;
	}

	public void setEndMicros(long endMicros) {
		this.endMicros = endMicros;
	}

}
