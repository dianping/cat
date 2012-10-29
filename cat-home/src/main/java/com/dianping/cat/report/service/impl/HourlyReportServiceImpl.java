package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.page.model.database.DatabaseReportMerger;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.page.model.matrix.MatrixReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.service.HourlyReportService;
import com.dianping.cat.report.task.health.HealthReportMerger;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class HourlyReportServiceImpl implements HourlyReportService {

	@Inject
	private ReportDao m_reportDao;

	private Set<String> queryAllDomainNames(Date start, Date end, String reportName) {
		Set<String> domains = new HashSet<String>();

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, null, reportName,
			      ReportEntity.READSET_DOMAIN_NAME);

			for (Report report : reports) {
				domains.add(report.getDomain());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return domains;
	}

	@Override
	public TransactionReport queryTransactionReport(String domain, Date start, Date end) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "transaction",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();

				try {
					TransactionReport reportModel = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser
					      .parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "transaction", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "transaction");
		transactionReport.getDomainNames().addAll(domains);
		return transactionReport;
	}

	@Override
	public EventReport queryEventReport(String domain, Date start, Date end) {
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "event",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();

				try {
					EventReport reportModel = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "event", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "event");
		eventReport.getDomainNames().addAll(domains);
		return eventReport;
	}

	@Override
	public ProblemReport queryProblemReport(String domain, Date start, Date end) {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "problem",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					ProblemReport reportModel = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser
					      .parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "problem", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		ProblemReport problemReport = merger.getProblemReport();

		problemReport.setStartTime(start);
		problemReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "problem");
		problemReport.getDomainNames().addAll(domains);
		return problemReport;
	}

	@Override
	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "heartbeat",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					HeartbeatReport reportModel = com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser
					      .parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logHeartbeat("ErrorXML", "heartbeat", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "heartbeat");
		heartbeatReport.getDomainNames().addAll(domains);
		return heartbeatReport;
	}

	@Override
	public MatrixReport queryMatrixReport(String domain, Date start, Date end) {
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "matrix",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "matrix", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "matrix");
		matrixReport.getDomainNames().addAll(domains);
		return matrixReport;
	}

	@Override
	public CrossReport queryCrossReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "cross",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					CrossReport reportModel = com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "cross", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "cross");
		crossReport.getDomainNames().addAll(domains);
		return crossReport;
	}

	@Override
	public SqlReport querySqlReport(String domain, Date start, Date end) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "sql",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					SqlReport reportModel = com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "sql", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		SqlReport sqlReport = merger.getSqlReport();

		sqlReport.setStartTime(start);
		sqlReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "sql");
		sqlReport.getDomainNames().addAll(domains);
		return sqlReport;
	}

	@Override
	public DatabaseReport queryDatabaseReport(String database, Date start, Date end) {
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(database));

		try {
			List<Report> reports = m_reportDao.findDatabaseAllByDomainNameDuration(start, end, database, "database",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					DatabaseReport reportModel = com.dianping.cat.consumer.database.model.transform.DefaultSaxParser
					      .parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "database", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		DatabaseReport databaseReport = merger.getDatabaseReport();

		databaseReport.setStartTime(start);
		databaseReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "database");
		databaseReport.getDomainNames().addAll(domains);
		return databaseReport;
	}

	@Override
	public HealthReport queryHealthReport(String domain, Date start, Date end) {
		HealthReportMerger merger = new HealthReportMerger(new HealthReport(domain));

		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(start, end, domain, "health",
			      ReportEntity.READSET_FULL);
			for (Report report : reports) {
				String xml = report.getContent();

				try {
					HealthReport reportModel = com.dianping.cat.consumer.health.model.transform.DefaultSaxParser.parse(xml);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "health", Event.SUCCESS, xml);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		HealthReport healthReport = merger.getHealthReport();

		healthReport.setStartTime(start);
		healthReport.setEndTime(end);

		Set<String> domains = queryAllDomainNames(start, end, "health");
		healthReport.getDomainNames().addAll(domains);
		return healthReport;
	}

}
