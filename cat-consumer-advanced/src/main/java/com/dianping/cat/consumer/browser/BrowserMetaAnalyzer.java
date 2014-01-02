package com.dianping.cat.consumer.browser;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.browsermeta.model.entity.BrowserMetaReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class BrowserMetaAnalyzer extends AbstractMessageAnalyzer<BrowserMetaReport> {
	public static final String ID = "browserMeta";

	@Inject(ID)
	protected ReportManager<BrowserMetaReport> m_reportManager;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public BrowserMetaReport getReport(String domain) {
		BrowserMetaReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	private String parseValue(final String key, final String data) {
		int len = data == null ? 0 : data.length();
		int keyLen = key.length();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean inName = true;

		for (int i = 0; i < len; i++) {
			char ch = data.charAt(i);

			switch (ch) {
			case '&':
				if (name.length() == keyLen && name.toString().equals(key)) {
					return value.toString();
				}
				inName = true;
				name.setLength(0);
				value.setLength(0);
				break;
			case '=':
				if (inName) {
					inName = false;
				} else {
					value.append(ch);
				}
				break;
			default:
				if (inName) {
					name.append(ch);
				} else {
					value.append(ch);
				}
				break;
			}
		}

		if (name.length() == keyLen && name.toString().equals(key)) {
			return value.toString();
		}

		return null;
	}

	@Override
	public void process(MessageTree tree) {
		Message message = tree.getMessage();
		if (message instanceof Transaction) {
			String type = message.getType();

			if ("URL".equals(type)) {
				List<Message> children = ((Transaction) message).getChildren();

				for (Message child : children) {
					String childType = child.getType();
					String childName = child.getName();

					if ("URL".equals(childType) && ("URL.Server".equals(childName) || "ClientInfo".equals(childName))) {
						String data = (String) child.getData();
						String agent = parseValue("Agent", data);

						if (agent != null) {
							String domain = tree.getDomain();
							BrowserMetaReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

							report.findOrCreateUserAgent(agent).incCount();
						}
						return;
					}
				}
			}
		}
	}
}
