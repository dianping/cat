package com.dianping.cat.consumer.failure;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Jan 5, 2012
 */
public class FailureAnalyzer extends AbstractMessageAnalyzer<FailureReport> implements Initializable, LogEnabled {

	private static final SimpleDateFormat FILE_SDF = new SimpleDateFormat("yyyyMMddHHmm");

	private static final long MINUTE = 60 * 1000;

	@Inject
	private MessageManager m_messageManager;

	@Inject
	private MessageStorage m_messageStorage;

	private Map<String, FailureReport> m_reports = new HashMap<String, FailureReport>();

	private long m_extraTime;

	private String m_reportPath;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	protected List<FailureReport> generate() {
		List<FailureReport> reports = new ArrayList<FailureReport>(m_reports.size());

		for (String domain : m_reports.keySet()) {
			FailureReport report = generate(domain);

			reports.add(report);
		}

		return reports;
	}

	FailureReport generate(String domain) {
		if (domain == null) {
			List<String> domains = getDomains();

			domain = domains.size() > 0 ? domains.get(0) : null;
		}

		FailureReport report = m_reports.get(domain);

		return report;
	}

	public List<String> getDomains() {
		List<String> domains = new ArrayList<String>(m_reports.keySet());

		Collections.sort(domains, new Comparator<String>() {
			@Override
			public int compare(String d1, String d2) {
				if (d1.equals("Cat")) {
					return 1;
				}

				return d1.compareTo(d2);
			}
		});

		return domains;
	}

	public FailureReport getReport(String domain) {
		return m_reports.get(domain);
	}

	public Map<String, FailureReport> getReports() {
		return m_reports;
	}

	private String getFailureFileName(FailureReport report) {
		StringBuffer result = new StringBuffer();
		String start = FILE_SDF.format(report.getStartTime());
		String end = FILE_SDF.format(report.getEndTime());

		result.append(report.getDomain()).append("-").append(start).append("-").append(end);
		return result.toString();
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_messageManager.getClientConfig();

		if (config != null) {
			Property property = config.findProperty("failure-base-dir");

			if (property != null) {
				m_reportPath = property.getValue();
			}
		}
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	@Override
	protected void process(MessageTree tree) {

	}

	public void setAnalyzerInfo(long startTime, long duration, String domain, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	public void setMessageStorage(MessageStorage messageStorage) {
		m_messageStorage = messageStorage;
	}

	public void setReportPath(String reportPath) {
		m_reportPath = reportPath;
	}

	@Override
	protected void store(List<FailureReport> reports) {
		if (reports == null || reports.size() == 0) {
			return;
		}

		for (FailureReport report : reports) {
			String failureFileName = getFailureFileName(report);
			String htmlPath = new StringBuilder().append(m_reportPath).append(failureFileName).append(".html").toString();
			File file = new File(htmlPath);

			file.getParentFile().mkdirs();

			try {
				Files.forIO().writeTo(file, new DefaultJsonBuilder().buildJson(report));
			} catch (IOException e) {
				m_logger.error(String.format("Error when writing to file(%s)!", file), e);
			}
		}
	}
}
