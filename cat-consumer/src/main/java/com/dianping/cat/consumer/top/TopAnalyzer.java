package com.dianping.cat.consumer.top;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> implements LogEnabled {
	public static final String ID = "top";

	@Inject(ID)
	private ReportManager<TopReport> m_reportManager;

	private ProblemAnalyzer m_problemAnalyzer;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			m_reportManager.getHourlyReport(startTime, Constants.CAT, true);
			m_reportManager.getHourlyReports(startTime).put(Constants.CAT, getReport(Constants.CAT));
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public TopReport getReport(String domain) {
		Set<String> domains = m_problemAnalyzer.getDomains();
		TopReport topReport = new TopReport(Constants.CAT);

		topReport.setStartTime(new Date(m_startTime));
		topReport.setEndTime(new Date(m_startTime + 60 * MINUTE - 1));


		ProblemReportVisitor problemReportVisitor = new ProblemReportVisitor(topReport);

		for (String name : domains) {
			try {
				if (m_serverConfigManager.validateDomain(name) || Constants.FRONT_END.equals(name)) {
					ProblemReport report = m_problemAnalyzer.getReport(name);

					problemReportVisitor.visitProblemReport(report);
				}
			} catch (ConcurrentModificationException e) {
				try {
					ProblemReport report = m_problemAnalyzer.getReport(name);

					problemReportVisitor.visitProblemReport(report);
				} catch (ConcurrentModificationException ce) {
					Cat.logEvent("ConcurrentModificationException", name, Event.SUCCESS, null);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return topReport;
	}

	@Override
	public boolean isRawAnalyzer() {
		return false;
	}

	@Override
	protected void process(MessageTree tree) {
	}

	public void setProblemAnalyzer(ProblemAnalyzer problemAnalyzer) {
		m_problemAnalyzer = problemAnalyzer;
	}

	public static class ProblemReportVisitor extends com.dianping.cat.consumer.problem.model.transform.BaseVisitor {
		private String m_domain;

		private String m_ip;

		private String m_type;

		private String m_state;

		private TopReport m_report;

		public ProblemReportVisitor(TopReport report) {
			m_report = report;
		}

		@Override
		public void visitEntity(Entity entity) {
			m_type = entity.getType();
			m_state = entity.getStatus();
			super.visitEntity(entity);
		}

		@Override
		public void visitMachine(Machine machine) {
			m_ip = machine.getIp();
			super.visitMachine(machine);
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			m_domain = problemReport.getDomain();
			super.visitProblemReport(problemReport);
		}

		@Override
		public void visitSegment(Segment segment) {
			int id = segment.getId();
			int count = segment.getCount();

			if ("error".equals(m_type)) {
				com.dianping.cat.consumer.top.model.entity.Segment segmentDetail = m_report.findOrCreateDomain(m_domain)
				      .findOrCreateSegment(id);
				segmentDetail.setError(segmentDetail.getError() + count);

				Error error = segmentDetail.findOrCreateError(m_state);

				error.setCount(error.getCount() + count);
				segmentDetail.findOrCreateMachine(m_ip).incCount(count);
			}
		}
	}

}