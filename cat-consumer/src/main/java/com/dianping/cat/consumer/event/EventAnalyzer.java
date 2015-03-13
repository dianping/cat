package com.dianping.cat.consumer.event;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class EventAnalyzer extends AbstractMessageAnalyzer<EventReport> implements LogEnabled {

	public static final String ID = "event";

	private EventTpsStatisticsComputer m_computer = new EventTpsStatisticsComputer();

	@Inject(ID)
	private ReportManager<EventReport> m_reportManager;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public EventReport getReport(String domain) {
		long period = getStartTime();
		long timestamp = System.currentTimeMillis();
		long remainder = timestamp % 3600000;
		long current = timestamp - remainder;
		EventReport report = m_reportManager.getHourlyReport(period, domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		if (period == current) {
			report.accept(m_computer.setDuration(remainder / 1000));
		} else if (period < current) {
			report.accept(m_computer.setDuration(3600));
		}
		return report;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (m_serverConfigManager.validateDomain(domain)) {
			EventReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				processTransaction(report, tree, (Transaction) message);
			} else if (message instanceof Event) {
				processEvent(report, tree, (Event) message);
			}
		}
	}

	private void processEvent(EventReport report, MessageTree tree, Event event) {
		int count = 1;
		String ip = tree.getIpAddress();
		EventType type = report.findOrCreateMachine(ip).findOrCreateType(event.getType());
		EventName name = type.findOrCreateName(event.getName());
		String messageId = tree.getMessageId();

		report.addIp(tree.getIpAddress());
		type.incTotalCount(count);
		name.incTotalCount(count);

		if (event.isSuccess()) {
			if (type.getSuccessMessageUrl() == null) {
				type.setSuccessMessageUrl(messageId);
			}

			if (name.getSuccessMessageUrl() == null) {
				name.setSuccessMessageUrl(messageId);
			}
		} else {
			type.incFailCount(count);
			name.incFailCount(count);

			if (type.getFailMessageUrl() == null) {
				type.setFailMessageUrl(messageId);
			}

			if (name.getFailMessageUrl() == null) {
				name.setFailMessageUrl(messageId);
			}
		}
		type.setFailPercent(type.getFailCount() * 100.0 / type.getTotalCount());
		name.setFailPercent(name.getFailCount() * 100.0 / name.getTotalCount());

		processEventGrpah(name, event, count);
	}

	private void processEventGrpah(EventName name, Event t, int count) {
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Range range = name.findOrCreateRange(min);

		range.incCount(count);
		if (!t.isSuccess()) {
			range.incFails(count);
		}
	}

	private void processTransaction(EventReport report, MessageTree tree, Transaction t) {
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child);
			}
		}
	}

	public void setReportManager(ReportManager<EventReport> reportManager) {
		m_reportManager = reportManager;
	}
}
