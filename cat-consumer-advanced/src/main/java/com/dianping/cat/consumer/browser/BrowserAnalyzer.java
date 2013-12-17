package com.dianping.cat.consumer.browser;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.consumer.browser.model.entity.UserAgent;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class BrowserAnalyzer extends AbstractMessageAnalyzer<BrowserReport> {
	public static final String ID = "browser";

	@Inject(ID)
	private ReportManager<BrowserReport> m_reportManager;

	@Inject
	private UserAgentManager m_userAgentManager;

	// public void removeMapItem(Map<String, Integer> map) {
	//
	// m_userAgents.put("domain", value );
	//
	// Map.Entry<String, Integer> itemRemoved = null;
	//
	// for (Map.Entry<String, Integer> item : map.entrySet()) {
	// if (itemRemoved == null) {
	// itemRemoved = item;
	// } else if (item.getValue() < itemRemoved.getValue()) {
	// itemRemoved = item;
	// }
	// }
	// map.remove(itemRemoved.getKey());
	// }

	@Override
	public void doCheckpoint(boolean atEnd) {
		// m_userAgentManager.storeResult();
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public BrowserReport getReport(String domain) {
		BrowserReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	protected String parseValue(final String key, final String data) {
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
					BrowserReport report = m_reportManager.getHourlyReport(getStartTime(), "Cat", true);

					if ("URL".equals(childType) && ("URL.Server".equals(childName) || "ClientInfo".equals(childName))) {
						String data = (String) child.getData();
						String domain = tree.getDomain();

						updateBrowserReport(report, domain, data);
						return;
					}
				}
			}
		}

	}

	private void updateBrowserReport(BrowserReport report, String domain, String data) {
		UserAgent userAgent = report.findOrCreateDomainDetail(domain).findOrCreateUserAgent(data);
		int count = 0;
		if(userAgent.getCount()!=null){
			count = userAgent.getCount();
		}
		userAgent.setCount(count + 1);
	}

	// private void updateBrowserReport(BrowserReport report, String domain,
	// String data) {
	// String agent = parseValue("Agent", data);
	//
	// if (agent == null || agent.isEmpty()) {
	// m_logger.error("Can not get agent from url when browser analyze: "
	// + data);
	// }
	//
	// UserAgent userAgent = m_userAgentManager.parse(agent);
	// String browserName = userAgent.getBrowser();
	// String osName = userAgent.getOs();
	// String versionName = userAgent.getVersion();
	// DomainDetail detail = report.findOrCreateDomainDetail(domain);
	//
	// Browser browser = detail.findOrCreateBrowser(browserName);
	// BrowserVersion version = browser
	// .findOrCreateBrowserVersion(versionName);
	// Os os = detail.findOrCreateOs(osName);
	//
	// browser.setCount(browser.getCount() + 1);
	// os.setCount(os.getCount() + 1);
	// version.setCount(version.getCount() + 1);
	// }
	
}

