package com.dianping.cat.consumer.ip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.consumer.ip.model.entity.AllDomains;
import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Period;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class IpAnalyzer extends AbstractMessageAnalyzer<IpReport> implements LogEnabled {
	private static final String TOKEN = "RemoteIP=";

	private Map<String, IpReport> m_reports = new HashMap<String, IpReport>();

	private int m_lastPhase;

	private Logger m_logger;

	private void clearLastPhase() {
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		int currentPhase = minute / 20; // 0, 1, 2

		if (m_lastPhase != currentPhase) {
			int baseIndex = m_lastPhase * 20;
			List<String> domains = new ArrayList<String>();

			for (Map.Entry<String, IpReport> e : m_reports.entrySet()) {
				IpReport report = e.getValue();
				Map<Integer, Period> periods = report.getPeriods();

				for (int i = 0; i < 20; i++) {
					periods.remove(baseIndex + i);
				}

				if (periods.isEmpty()) {
					domains.add(e.getKey());
				}
			}

			for (String domain : domains) {
				m_reports.remove(domain);
			}

			m_lastPhase = currentPhase;
		}
	}

	private IpReport findOrCreateReport(String domain) {
		IpReport report = m_reports.get(domain);

		if (report == null) {
			synchronized (m_reports) {
				report = m_reports.get(domain);

				if (report == null) {
					report = new IpReport().setDomain(domain);
					m_reports.put(domain, report);
				}
			}
		}

		return report;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	private String getIpAddress(Transaction root) {
		List<Message> children = ((Transaction) root).getChildren();

		for (Message child : children) {
			if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("ClientInfo")) {
				// URL:ClientInfo RemoteIp=<ip>&...
				String data = child.getData().toString();
				int off = data.indexOf(TOKEN);

				if (off >= 0) {
					int pos = data.indexOf('&', off + TOKEN.length());

					if (pos > 0) {
						return data.substring(off + TOKEN.length(), pos);
					}
				}

				break;
			} else if (child instanceof Heartbeat) {
				// Heartbeat:<ip>
				return child.getName();
			}
		}

		return null;
	}

	public IpReport getReport(String domain) {
		IpReport report = m_reports.get(domain);

		if (report == null) {
			report = new IpReport();
		}

		List<String> sortedDomains = sortDomains(m_reports.keySet());
		AllDomains allDomains = new AllDomains();

		for (String e : sortedDomains) {
			allDomains.addDomain(e);
		}

		report.setAllDomains(allDomains);
		return report;
	}

	@Override
	protected boolean isTimeout() {
		return false;
	}

	@Override
	protected void process(MessageTree tree) {
		Message root = tree.getMessage();

		if (root instanceof Transaction) {
			String address = getIpAddress((Transaction) root);

			if (address == null) {
				address = "N/A";

				m_logger.info("Unable to find IP address from message: " + tree);
			}

			String domain = tree.getDomain();
			Calendar cal = Calendar.getInstance();

			cal.setTimeInMillis(root.getTimestamp());

			int minute = cal.get(Calendar.MINUTE);
			IpReport report = findOrCreateReport(domain);
			Period period = report.findOrCreatePeriod(minute);
			Ip ip = period.findOrCreateIp(address);

			ip.incCount();

			clearLastPhase();
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
