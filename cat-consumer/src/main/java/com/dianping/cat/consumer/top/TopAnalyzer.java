package com.dianping.cat.consumer.top;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> implements LogEnabled {
	public static final String ID = "top";

	@Inject(ID)
	private ReportManager<TopReport> m_reportManager;

	@Inject
	private Set<String> m_errorTypes;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public TopReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, false);
	}

	@Override
	public ReportManager<TopReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			TopReport report = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				processTransaction(report, tree, (Transaction) message);
			} else if (message instanceof Event) {
				processEvent(report, tree, (Event) message);
			}
		}
	}

	private void processEvent(TopReport report, MessageTree tree, Event event) {
		String type = event.getType();

		if (m_errorTypes.contains(type)) {
			String domain = tree.getDomain();
			String ip = tree.getIpAddress();
			String exception = event.getName();
			long current = event.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateDomain(domain).findOrCreateSegment(min).incError();

			segment.findOrCreateError(exception).incCount();
			segment.findOrCreateMachine(ip).incCount();
		}
	}

	private void processTransaction(TopReport report, MessageTree tree, Transaction t) {
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child);
			}
		}
	}

	public void setErrorType(String type) {
		m_errorTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
	}
}