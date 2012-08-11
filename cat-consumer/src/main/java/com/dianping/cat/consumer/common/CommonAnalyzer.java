package com.dianping.cat.consumer.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.cat.consumer.model.entity.CommonReport;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class CommonAnalyzer extends AbstractMessageAnalyzer<CommonReport> implements LogEnabled {

	private Map<String, CommonReport> m_reports = new HashMap<String, CommonReport>();

	@Override
	public void doCheckpoint(boolean atEnd) {
		if(atEnd){
			storeReport();
		}
	}

	//TODO
	private void storeReport() {
   }

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		CommonReport report = m_reports.get(domain);

		if (report == null) {
			report = new CommonReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		String ip = tree.getIpAddress();
		report.getIps().add(ip);
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

	@Override
	public CommonReport getReport(String domain) {
		throw new RuntimeException("Can't invoke get report in common anayler!");
	}
}
