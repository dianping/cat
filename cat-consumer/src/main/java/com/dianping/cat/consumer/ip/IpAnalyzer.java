package com.dianping.cat.consumer.ip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Segment;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class IpAnalyzer extends AbstractMessageAnalyzer<IpReport> {
	private Map<String, IpReport> m_reports = new HashMap<String, IpReport>();

	private static final String TOKEN = "RemoteIP=";

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
	public List<IpReport> generate() {
		return null;
	}

	public IpReport generate(String domain) {
		IpReport report = null;

		if (domain == null) {
			if (!m_reports.isEmpty()) {
				domain = m_reports.keySet().iterator().next();
			}
		}

		if (domain != null) {
			report = m_reports.get(domain);
		}

		return report;
	}

	private String getIpAddress(Transaction root) {
		List<Message> children = ((Transaction) root).getChildren();

		for (Message child : children) {
			if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("ClientInfo")) {
				String data = child.getData().toString();
				int off = data.indexOf(TOKEN);

				if (off >= 0) {
					int pos = data.indexOf('&', off + TOKEN.length());

					if (pos > 0) {
						return data.substring(off + TOKEN.length(), pos);
					}
				}

				break;
			}
		}

		return null;
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
			}

			String domain = tree.getDomain();
			Calendar cal = Calendar.getInstance();

			cal.setTimeInMillis(root.getTimestamp());

			int minute = cal.get(Calendar.MINUTE);
			IpReport report = findOrCreateReport(domain);
			Segment segment = report.findOrCreateSegment(minute);
			Ip ip = segment.findOrCreateIp(address);

			ip.setCount(ip.getCount() + 1);
		}
	}

	@Override
	protected void store(List<IpReport> reports) {
		// TODO Auto-generated method stub

	}
}
