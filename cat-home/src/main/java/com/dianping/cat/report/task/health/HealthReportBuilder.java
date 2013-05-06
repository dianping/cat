package com.dianping.cat.report.task.health;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.home.dal.report.Monthreport;
import com.dianping.cat.home.dal.report.Weeklyreport;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.transaction.MergeAllMachine;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.health.HealthServiceCollector.ServiceInfo;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class HealthReportBuilder extends AbstractReportBuilder implements ReportBuilder, LogEnabled {

	@Inject
	private HealthServiceCollector m_serviceCollector;

	private Logger m_logger;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		HealthReportMerger merger = new HealthReportMerger(new HealthReport(reportDomain));

		List<Report> reports = null;
		try {
			reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDomain, reportName,
			      ReportEntity.READSET_FULL);
		} catch (DalException e1) {
			Cat.logError(e1);
		}
		if (reports != null) {
			for (Report report : reports) {
				String xml = report.getContent();
				try {
					HealthReport model = com.dianping.cat.consumer.health.model.transform.DefaultSaxParser.parse(xml);
					model.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
		String content = merger.getHealthReport().toString();

		Dailyreport report = m_dailyReportDao.createLocal();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_dailyReportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return true;
	}

	private HealthReport buildHealthHourlyReport(String reportDomain, Date reportPeriod) {
		TransactionReport transactionReport = queryTransactionReport(reportDomain, reportPeriod);
		EventReport eventReport = queryEventReport(reportDomain, reportPeriod);
		ProblemReport problemReport = queryProblemReport(reportDomain, reportPeriod);
		HeartbeatReport heartbeatReport = queryHeartbeatReport(reportDomain, reportPeriod);

		m_serviceCollector.buildCrossInfo(reportPeriod.getTime());
		Map<String, ServiceInfo> infos = m_serviceCollector.getServiceInfos();

		HealthReportCreator healthReportCreator = new HealthReportCreator();
		HealthReport report = healthReportCreator.build(transactionReport, eventReport, problemReport, heartbeatReport,
		      infos);
		Set<String> domains = getDomainsFromHourlyReport(reportPeriod, new Date(reportPeriod.getTime()
		      + TimeUtil.ONE_HOUR));
		report.getDomainNames().addAll(domains);
		return report;
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		try {
			HealthReport report = buildHealthHourlyReport(reportDomain, reportPeriod);

			DefaultXmlBuilder builder = new DefaultXmlBuilder(true);

			Report r = m_reportDao.createLocal();
			String xml = builder.buildXml(report);

			r.setName("health");
			r.setDomain(reportDomain);
			r.setPeriod(reportPeriod);
			r.setIp(ip);
			r.setType(1);
			r.setContent(xml);

			m_reportDao.insert(r);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private HealthReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HealthReportMerger merger = new HealthReportMerger(new HealthReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				Dailyreport dailyreport = m_dailyReportDao.findByNameDomainPeriod(new Date(startTime), domain, "health",
				      DailyreportEntity.READSET_FULL);
				String xml = dailyreport.getContent();

				HealthReport reportModel = com.dianping.cat.consumer.health.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HealthReport healthReport = merger.getHealthReport();
		healthReport.setStartTime(start);
		healthReport.setEndTime(end);
		return healthReport;
	}

	@Override
	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.MONTH, 1);

		Date start = reportPeriod;
		Date end = cal.getTime();

		HealthReport healthReport = buildMergedDailyReport(reportDomain, start, end);
		Monthreport report = m_monthreportDao.createLocal();

		report.setContent(healthReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_monthreportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod) {
		Date start = reportPeriod;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		HealthReport healthReport = buildMergedDailyReport(reportDomain, start, end);
		Weeklyreport report = m_weeklyreportDao.createLocal();
		String content = healthReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDomain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);

		try {
			m_weeklyreportDao.insert(report);
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private EventReport queryEventReport(String domain, Date reportPeriod) {
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));
		EventReport eventReport = merger.getEventReport();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "event",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();
				EventReport model = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
				eventReport.getDomainNames().addAll(model.getDomainNames());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		com.dianping.cat.report.page.event.MergeAllMachine all = new com.dianping.cat.report.page.event.MergeAllMachine();
		all.visitEventReport(eventReport);
		eventReport = all.getReport();


		eventReport.setStartTime(reportPeriod);
		eventReport.setEndTime(new Date(reportPeriod.getTime() + TimeUtil.ONE_HOUR));
		return eventReport;
	}

	private HeartbeatReport queryHeartbeatReport(String domain, Date reportPeriod) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "heartbeat",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();
				HeartbeatReport model = com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
				heartbeatReport.getDomainNames().addAll(model.getDomainNames());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		heartbeatReport.setStartTime(reportPeriod);
		heartbeatReport.setEndTime(new Date(reportPeriod.getTime() + TimeUtil.ONE_HOUR));
		return heartbeatReport;
	}

	private ProblemReport queryProblemReport(String domain, Date reportPeriod) {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));
		ProblemReport problemReport = merger.getProblemReport();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "problem",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();
				ProblemReport model = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
				model.accept(merger);
				problemReport.getDomainNames().addAll(model.getDomainNames());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		problemReport.setStartTime(reportPeriod);
		problemReport.setEndTime(new Date(reportPeriod.getTime() + TimeUtil.ONE_HOUR));
		return problemReport;
	}

	private TransactionReport queryTransactionReport(String domain, Date reportPeriod) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		TransactionReport transactionReport = merger.getTransactionReport();
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "transaction",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();
				TransactionReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
				transactionReport.getDomainNames().addAll(model.getDomainNames());
			}
		} catch (Exception e) {
			m_logger.error(domain + " " + reportPeriod, e);
			Cat.logError(e);
		}
		MergeAllMachine all = new MergeAllMachine();
		all.visitTransactionReport(transactionReport);
		transactionReport = all.getReport();

		transactionReport.setStartTime(reportPeriod);
		transactionReport.setEndTime(new Date(reportPeriod.getTime() + TimeUtil.ONE_HOUR));
		return transactionReport;
	}

}
