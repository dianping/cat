package com.dianping.cat.notify.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.notify.dao.DailyReportDao;
import com.dianping.cat.notify.model.DailyReport;
import com.dianping.cat.notify.model.entity.Report;
import com.dianping.cat.notify.render.IRender;
import com.dianping.cat.notify.server.ContainerHolder;
import com.dianping.cat.notify.util.TimeUtil;

public abstract class AbstractReportCreater implements ReportCreater {
	
	private static final String TRENDS_URL= "<a href='http://cat.dianpingoa.com/cat/r/%s?op=historyGraph&domain=%s&date=%s&ip=All&reportType=%s&type=%s' target='_blank'>%s</a>";
   
	private static final String CURRENT_URL="<a href='http://cat.dianpingoa.com/cat/r/%s?domain=%s&date=%s&reportType=&op=view' target='_blank'>%s</a>";
	
	private final static Logger logger = LoggerFactory.getLogger(AbstractReportCreater.class);

	protected ReportConfig m_config;

	protected ContainerHolder m_configHolder;

	protected DailyReportDao m_dailyReportDao;

	protected IRender m_render;

	protected AtomicLong lastSuccessTime = new AtomicLong();

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
		List<DailyReport> dailyReportList = null;
		try {
			dailyReportList = m_dailyReportDao.findSendMailReportDomainDuration(new Date(startMicros), new Date(endMicros), domain, DailyReport.XML_TYPE);
		} catch (Exception e) {
			logger.error(String.format("fail to read report from database,time range[%s,%s]", new Date(startMicros),
			      new Date(endMicros)), e);
			return null;
		}

		if (dailyReportList == null || dailyReportList.size() == 0) {
			logger.error(String.format("read empty data from database,time range[%s,%s]", new Date(startMicros), new Date(
			      endMicros)));
			return null;
		}

		Map<String, List<DailyReport>> nameToDailyReportsMap = new HashMap<String, List<DailyReport>>();

		for (DailyReport report : dailyReportList) {
			List<DailyReport> reportList = nameToDailyReportsMap.get(report.getName());
			if (reportList == null) {
				reportList = new ArrayList<DailyReport>();
				nameToDailyReportsMap.put(report.getName(), reportList);
			}
			reportList.add(report);
		}

		StringBuilder report_content = new StringBuilder();
		for (Map.Entry<String, List<DailyReport>> reportGroup : nameToDailyReportsMap.entrySet()) {
			String reportName = reportGroup.getKey();
			if (reportName.equals(DailyReport.EVENT_REPORT)) {
				EventReport eReport = parseEvent(reportGroup.getValue(), domain);
				report_content.append(renderEventReport(timeRange, eReport, domain));
			} else if (reportName.equals(DailyReport.PROBLEM_REPORT)) {
				ProblemReport pReport = parseProblem(reportGroup.getValue(), domain);
				report_content.append(renterProblemReport(timeRange, pReport, domain));
			} else if (reportName.equals(DailyReport.TRANSACGION_REPORT)) {
				TransactionReport tReport = parseTransction(reportGroup.getValue(), domain);
				caculateTps(tReport, ReportConstants.ALL_IP);
				report_content.append(renderTransactionReport(timeRange, tReport, domain));
			}
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
			if(xml == null){
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
			if(xml == null){
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
			if(xml == null){
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
	//http://cat.dianpingoa.com/cat/r/%s?op=historyGraph&domain=%s&date=%s&ip=All&reportType=%s&type=%s'>%s
	protected String getTrendsViewUrl(String reportType, String domain, long timestamp,String dayOrWeak,String name,String hyperText) {
		return String.format(TRENDS_URL, reportType, domain, TimeUtil.formatTime("yyyyMMdd", timestamp),dayOrWeak,name,hyperText);
	}
	
	//http://cat.dianpingoa.com/cat/r/%s?domain=%s&date=%s&reportType=&op=view
	protected String getCurrentViewUrl(String reportType, String domain, long timestamp){
		String reportDay = TimeUtil.formatTime("yyyy-MM-dd", timestamp);
		return String.format(CURRENT_URL, reportType, domain, TimeUtil.formatTime("yyyyMMdd", timestamp),reportDay);

	}

	private void caculateTps(TransactionReport report, String ip) {
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
			return;
		}
		for (TransactionType transType : machine.getTypes().values()) {
			long totalCount = transType.getTotalCount();
			double tps = 0;
			double time = (report.getEndTime().getTime() - report
					.getStartTime().getTime()) / (double) 1000;
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
