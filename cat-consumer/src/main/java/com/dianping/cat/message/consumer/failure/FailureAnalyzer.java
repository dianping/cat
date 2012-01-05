package com.dianping.cat.message.consumer.failure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;

public class FailureAnalyzer extends AbstractMessageAnalyzer<FailureReport>
		implements Initializable {
	@Inject
	private File m_reportFile;

	@Inject
	private List<Handler> m_handlers;

	private FailureReport m_report;

	@Override
	public FailureReport generate() {
		return m_report;
	}

	@Override
	public void initialize() throws InitializationException {
		m_report = new FailureReport();
	}

	@Override
	protected void process(MessageTree tree) {
		for (Handler handler : m_handlers) {
			handler.handle(m_report, tree);
		}
	}

	public void setReportFile(File reportFile) {
		m_reportFile = reportFile;
	}

	public void addHandlers(Handler handler) {
		if (m_handlers == null) {
			m_handlers = new ArrayList<FailureAnalyzer.Handler>();
		}

		m_handlers.add(handler);
	}

	@Override
	protected void store(FailureReport report) {
		String content = report.toString();

		try {
			Files.forIO().writeTo(m_reportFile, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static interface Handler {
		public void handle(FailureReport report, MessageTree tree);
	}

	public static class ErrorHandler implements Handler {
		@Override
		public void handle(FailureReport report, MessageTree tree) {

		}
	}

	public static class ExceptionHandler implements Handler {
		@Override
		public void handle(FailureReport report, MessageTree tree) {

		}
	}

	public static class LongUrlHandler implements Handler {
		@Inject
		private long m_threshold;

		@Override
		public void handle(FailureReport report, MessageTree tree) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				Transaction t = (Transaction) message;

				if (t.getDuration() > m_threshold) {
					// TODO
				}
			}
		}

		public void setThreshold(long threshold) {
			m_threshold = threshold;
		}
	}
}
