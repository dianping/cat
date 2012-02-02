package com.dianping.cat.consumer.failure;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.consumer.failure.model.entity.Entry;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Machines;
import com.dianping.cat.consumer.failure.model.entity.Segment;
import com.dianping.cat.consumer.failure.model.entity.Threads;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public class FailureReportAnalyzer extends AbstractMessageAnalyzer<FailureReport> implements Initializable {
	@Inject
	private List<Handler> m_handlers;

	@Inject
	private String m_reportPath;

	@Inject
	private MessageManager m_manager;

	private Map<String, FailureReport> m_reports = new HashMap<String, FailureReport>();

	private long m_startTime;

	private long m_extraTime;

	private long m_duration;

	private static final long MINUTE = 60 * 1000;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static final SimpleDateFormat FILE_SDF = new SimpleDateFormat("yyyyMMddHHmm");

	public void setAnalyzerInfo(long startTime, long duration, String domain, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	private FailureReport getReportByDomain(String domain) {
		FailureReport report = m_reports.get(domain);
		if (report != null) {
			return report;
		}

		FailureReport addedReport = new FailureReport();
		addedReport.setStartTime(new Date(m_startTime));
		addedReport.setEndTime(new Date(m_startTime + m_duration - MINUTE));
		addedReport.setDomain(domain);
		addedReport.setMachines(new Machines());
		addedReport.setThreads(new Threads());
		m_reports.put(domain, addedReport);
		return addedReport;
	}

	public void addHandlers(Handler handler) {
		if (m_handlers == null) {
			m_handlers = new ArrayList<FailureReportAnalyzer.Handler>();
		}

		m_handlers.add(handler);
	}

	@Override
	public List<FailureReport> generate() {
		List<FailureReport> reports = new ArrayList<FailureReport>();
		for (String domain : m_reports.keySet()) {
			reports.add(generateByDomain(domain));
		}
		return reports;
	}
	

	@Override
	public FailureReport generate(String domain) {
		return generateByDomain(domain);
	}

	@Override
	protected void store(List<FailureReport> reports) {
		if (reports != null) {
			for (FailureReport report : reports) {
				String failureFileName = getFailureFileName(report);
				String htmlPath = new StringBuilder().append(m_reportPath).append(failureFileName).append(".html")
				      .toString();
				File file = new File(htmlPath);

				file.getParentFile().mkdirs();
				FailureReportStore.storeToHtml(file, report);
			}
		}
	}

	public FailureReport generateByDomain(String domain) {
		FailureReport m_report = getReportByDomain(domain);
		long time = System.currentTimeMillis();
		long endTime = time - time % (60 * 1000);
		long start = m_report.getStartTime().getTime();

		long reportEndTime = m_report.getEndTime().getTime();
		if (reportEndTime < endTime)
			endTime = reportEndTime;
		Map<String, Segment> oldSegments = m_report.getSegments();
		Map<String, Segment> newSegments = new LinkedHashMap<String, Segment>();

		for (; start <= endTime; start = start + 60 * 1000) {
			String minute = SDF.format(new Date(start));
			Segment segment = oldSegments.get(minute);
			if (segment != null) {
				newSegments.put(minute, segment);
			} else {
				newSegments.put(minute, new Segment(minute));
			}
		}
		oldSegments.clear();
		for (String key : newSegments.keySet()) {
			oldSegments.put(key, newSegments.get(key));
		}
		return m_report;
	}

	@Override
	protected void process(MessageTree tree) {
		if (m_handlers == null) {
			throw new RuntimeException();
		}
		String domain = tree.getDomain();
		FailureReport report = getReportByDomain(domain);

		report.getMachines().addMachine(tree.getIpAddress());
		report.getThreads().addThread(tree.getThreadId());

		for (Handler handler : m_handlers) {
			handler.handle(report, tree);
		}
	}

	public void setReportPath(String configPath) {
		m_reportPath = configPath;
	}

	public String getReportPath() {
		return m_reportPath;
	}

	public String getFailureFileName(FailureReport report) {
		StringBuffer result = new StringBuffer();
		String start = FILE_SDF.format(report.getStartTime());
		String end = FILE_SDF.format(report.getEndTime());

		result.append(report.getDomain()).append("-").append(start).append("-").append(end);
		return result.toString();
	}

	@Override
	protected boolean isTimeout() {
		long endTime = m_startTime + m_duration + m_extraTime;
		long currentTime = System.currentTimeMillis();

		if (currentTime > endTime + m_extraTime) {
			return true;
		}
		return false;
	}

	public static interface Handler {
		public void handle(FailureReport report, MessageTree tree);
	}

	public static abstract class AbstractHandler implements Handler {

		@Inject
		private MessageStorage m_storage;

		public void setMessageStorage(MessageStorage storage) {
			m_storage = storage;
		}

		protected Segment findOrCreateSegment(Message message, FailureReport report) {
			long time = message.getTimestamp();
			long segmentId = time - time % MINUTE;
			String segmentStr = getDateFormat(segmentId);

			Map<String, Segment> segments = report.getSegments();
			Segment segment = segments.get(segmentStr);
			if (segment == null) {
				segment = new Segment(segmentStr);
				segments.put(segmentStr, segment);
			}

			return segment;
		}

		private String getDateFormat(long time) {
			String result = "2012-01-01 00:00";
			try {
				Date date = new Date(time);
				result = SDF.format(date);
			} catch (Exception e) {

			}
			return result;
		}

		private Entry getEntry(MessageTree tree) {
			String url = m_storage.store(tree);

			Entry entry = new Entry();
			entry.setPath(url);
			entry.setThreadId(tree.getThreadId());
			return entry;
		}
	}

	public static class FailureHandler extends AbstractHandler {
		@Inject
		private Set<String> m_failureTypes;

		private void addEntry(FailureReport report, Message message, MessageTree tree) {
			Entry entry = super.getEntry(tree);

			entry.setText(message.getName());
			entry.setType(message.getType());

			Segment segment = super.findOrCreateSegment(message, report);
			segment.addEntry(entry);
		}

		@Override
		public void handle(FailureReport report, MessageTree tree) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				Transaction transaction = (Transaction) message;

				processTransaction(report, transaction, tree);
			} else if (message instanceof Event) {
				processEvent(report, message, tree);
			}
		}

		private void processEvent(FailureReport report, Message message, MessageTree tree) {
			if (m_failureTypes.contains(message.getType())) {
				addEntry(report, message, tree);
			}
		}

		private void processTransaction(FailureReport report, Transaction transaction, MessageTree tree) {
			if (m_failureTypes.contains(transaction.getType())) {
				addEntry(report, transaction, tree);
			}

			List<Message> messageList = transaction.getChildren();
			for (Message message : messageList) {
				if (message instanceof Transaction) {
					Transaction temp = (Transaction) message;

					processTransaction(report, temp, tree);
				} else if (message instanceof Event) {
					processEvent(report, message, tree);
				}
			}
		}

		public void setFailureType(String type) {
			m_failureTypes = new HashSet<String>(Splitters.by(',').noEmptyItem().split(type));
		}
	}

	public static class LongUrlHandler extends AbstractHandler {
		@Inject
		private long m_threshold;

		private static final String LONG_URL = "LongUrl";

		@Override
		public void handle(FailureReport report, MessageTree tree) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				Transaction t = (Transaction) message;

				if (t.getDuration() > m_threshold) {
					Entry entry = super.getEntry(tree);

					entry.setText(message.getType() + "." + message.getName());
					entry.setType(LONG_URL);

					Segment segment = super.findOrCreateSegment(message, report);
					segment.addEntry(entry);
				}
			}
		}

		public void setThreshold(long threshold) {
			m_threshold = threshold;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_manager.getClientConfig();

		if (config != null) {
			Property property = config.findProperty("failure-base-dir");

			if (property != null) {
				m_reportPath = property.getValue();
			}
		}
	}
}
