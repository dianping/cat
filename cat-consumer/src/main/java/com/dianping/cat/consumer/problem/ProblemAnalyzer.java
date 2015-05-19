package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled, Initializable {
	public static final String ID = "problem";

	@Inject(ID)
	private ReportManager<ProblemReport> m_reportManager;

	@Inject
	private List<ProblemHandler> m_handlers;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public int getAnanlyzerCount() {
		return 2;
	}

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	@Override
	public ProblemReport getReport(String domain) {
		ProblemReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));

		return report;
	}

	@Override
	public ReportManager<ProblemReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void initialize() throws InitializationException {
		// to work around a performance issue within plexus
		m_handlers = new ArrayList<ProblemHandler>(m_handlers);
	}

	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		ProblemReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

		report.addIp(tree.getIpAddress());
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());

		for (ProblemHandler handler : m_handlers) {
			handler.handle(machine, tree);
		}
	}

}
