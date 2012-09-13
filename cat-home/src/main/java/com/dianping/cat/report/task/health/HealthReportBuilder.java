package com.dianping.cat.report.task.health;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.health.HealthServiceCollector.ServiceInfo;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class HealthReportBuilder extends AbstractReportBuilder implements ReportBuilder {

	@Inject
	private HealthServiceCollector m_serviceCollector;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {

		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = new HashSet<String>();
		getDomainSet(domainSet, reportPeriod, endDate);
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
		return false;
	}

	private HealthReport buildHealthHourlyReport(String reportDomain, Date reportPeriod) throws DalException,
	      SAXException, IOException {
		TransactionReport transactionReport = queryTransactionReport(reportDomain, reportPeriod);
		EventReport eventReport = queryEventReport(reportDomain, reportPeriod);
		ProblemReport problemReport = queryProblemReport(reportDomain, reportPeriod);
		HeartbeatReport heartbeatReport = queryHeartbeatReport(reportDomain, reportPeriod);

		m_serviceCollector.buildCrossInfo(reportPeriod.getTime());
		Map<String,ServiceInfo> infos = m_serviceCollector.getServiceInfos();

		HealthReportCreator healthReportCreator = new HealthReportCreator();
		HealthReport report = healthReportCreator.build(transactionReport, eventReport, problemReport, heartbeatReport,infos);
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
			String domain = report.getDomain();

			r.setName("health");
			r.setDomain(domain);
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

	private EventReport queryEventReport(String domain, Date reportPeriod) throws DalException, SAXException,
	      IOException {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "event",
		      ReportEntity.READSET_FULL);
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));
		merger.setAllIp(true);

		for (Report report : reports) {
			String xml = report.getContent();
			EventReport model = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		EventReport eventReport = merger.getEventReport();
		return eventReport;
	}

	private HeartbeatReport queryHeartbeatReport(String domain, Date reportPeriod) throws DalException, SAXException,
	      IOException {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "heartbeat",
		      ReportEntity.READSET_FULL);
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model = com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();
		return heartbeatReport;
	}

	private ProblemReport queryProblemReport(String domain, Date reportPeriod) throws DalException, SAXException,
	      IOException {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "problem",
		      ReportEntity.READSET_FULL);
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		ProblemReport problemReport = merger.getProblemReport();
		return problemReport;
	}

	private TransactionReport queryTransactionReport(String domain, Date reportPeriod) throws DalException,
	      SAXException, IOException {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(reportPeriod, domain, 1, "transaction",
		      ReportEntity.READSET_FULL);
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		merger.setAllIp(true);

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		TransactionReport transactionReport = merger.getTransactionReport();
		return transactionReport;
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		return true;
	}

	@Override
	public boolean redoHourReport(String reportName, String reportDomain, Date reportPeriod) {
		return true;
	}

}
