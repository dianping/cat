package com.dianping.cat.consumer.event;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

@Named(type = MessageAnalyzer.class, value = EventAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class EventAnalyzer extends AbstractMessageAnalyzer<EventReport> implements LogEnabled {

	public static final String ID = "event";

	@Inject(ID)
	private ReportManager<EventReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private EventTpsStatisticsComputer m_computer = new EventTpsStatisticsComputer();

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
	public EventReport getReport(String domain) {
		long period = getStartTime();
		long timestamp = System.currentTimeMillis();
		long remainder = timestamp % 3600000;
		long current = timestamp - remainder;
		EventReport report = m_reportManager.getHourlyReport(period, domain, false);

		if (period == current) {
			report.accept(m_computer.setDuration(remainder / 1000));
		} else if (period < current) {
			report.accept(m_computer.setDuration(3600));
		}
		return report;
	}

	@Override
	public ReportManager<EventReport> getReportManager() {
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
			EventReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
			String ip = tree.getIpAddress();
			List<Event> events = tree.getEvents();

			for (Event e : events) {
				String data = String.valueOf(e.getData());
				int total = 1;
				int fail = 0;
				boolean batchData = data.length() > 0 && data.charAt(0) == CatConstants.BATCH_FLAG;

				if (batchData) {
					String[] tab = data.substring(1).split(CatConstants.SPLIT);

					total = Integer.parseInt(tab[0]);
					fail = Integer.parseInt(tab[1]);
				} else {
					if (!e.isSuccess()) {
						fail = 1;
					}
				}
				processEvent(report, tree, e, ip);
			}
		}
	}

	private void processEvent(EventReport report, MessageTree tree, Event event, String ip) {
		int count = 1;
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

	public void setReportManager(ReportManager<EventReport> reportManager) {
		m_reportManager = reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		if (tree.getEvents().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

}
