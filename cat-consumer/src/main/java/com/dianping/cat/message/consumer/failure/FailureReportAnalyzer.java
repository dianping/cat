package com.dianping.cat.message.consumer.failure;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.model.failure.entity.Entry;
import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.entity.Machines;
import com.dianping.cat.consumer.model.failure.entity.Segment;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public class FailureReportAnalyzer extends
		AbstractMessageAnalyzer<FailureReport> {
	@Inject
	private List<Handler> m_handlers;

	@Inject
	private String m_reportPath;

	private FailureReport m_report;

	private long m_extraTime;

	private static final long MINUTE = 60 * 1000;

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	private static final SimpleDateFormat FILE_SDF = new SimpleDateFormat(
			"yyyyMMddHHmm");

	public void setAnalyzerInfo(long startTime, long duration, String domain,
			long extraTime) {
		m_report = new FailureReport();
		m_report.setStartTime(new Date(startTime));
		m_report.setEndTime(new Date(startTime + duration - MINUTE));
		m_report.setDomain(domain);
		m_extraTime = extraTime;
		m_report.setMachines(new Machines());
	}

	public void addHandlers(Handler handler) {
		if (m_handlers == null) {
			m_handlers = new ArrayList<FailureReportAnalyzer.Handler>();
		}
		m_handlers.add(handler);
	}

	@Override
	public FailureReport generate() {
		return m_report;
	}

	@Override
	protected void process(MessageTree tree) {
		if (m_handlers == null) {
			throw new RuntimeException();
		}

		m_report.getMachines().addMachine(tree.getIpAddress());
		for (Handler handler : m_handlers) {
			handler.handle(m_report, tree);
		}
	}

	public void setFailureReport(FailureReport report) {
		m_report = report;
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

		result.append(report.getDomain()).append("-").append(start).append("-")
				.append(end);
		return result.toString();
	}

	@Override
	protected void store(FailureReport report) {
		String failureFileName = getFailureFileName(report);
		String htmlPath = new StringBuilder().append(m_reportPath)
				.append(failureFileName).append(".html").toString();
		File file = new File(htmlPath);
		
		file.getParentFile().mkdirs();
		FailureReportStore.storeToHtml(file, report);
	}

	@Override
	protected boolean isTimeout() {
		long endTime = m_report.getEndTime().getTime();
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
		protected Segment findOrCreateSegment(Message message,
				FailureReport report) {
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
	}

	public static class FailureHandler extends AbstractHandler {
		@Inject
		private Set<String> m_failureTypes;

		private void addEntry(FailureReport report, Message message,
				MessageTree tree) {

			String messageId = tree.getMessageId();
			String threadId = tree.getThreadId();
			Entry entry = new Entry();

			entry.setMessageId(messageId);
			entry.setThreadId(threadId);
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

		private void processEvent(FailureReport report, Message message,
				MessageTree tree) {
			if (m_failureTypes.contains(message.getType())) {
				addEntry(report, message, tree);
			}
		}

		private void processTransaction(FailureReport report,
				Transaction transaction, MessageTree tree) {
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
			m_failureTypes = new HashSet<String>(Splitters.by(',')
					.noEmptyItem().split(type));
		}
	}

	public static class LongUrlHandler extends AbstractHandler {
		@Inject
		private long m_threshold;

		@Override
		public void handle(FailureReport report, MessageTree tree) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				String messageId = ((DefaultMessageTree) tree).getMessageId();
				String threadId = ((DefaultMessageTree) tree).getThreadId();
				Transaction t = (Transaction) message;

				if (t.getDuration() > m_threshold) {
					Entry entry = new Entry();

					entry.setMessageId(messageId);
					entry.setThreadId(threadId);
					entry.setText(message.getName());
					entry.setType(message.getType());

					Segment segment = super
							.findOrCreateSegment(message, report);
					segment.addEntry(entry);
				}
			}
		}

		public void setThreshold(long threshold) {
			m_threshold = threshold;
		}
	}
}
