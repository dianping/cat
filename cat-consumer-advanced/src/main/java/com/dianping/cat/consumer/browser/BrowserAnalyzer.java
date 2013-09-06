package com.dianping.cat.consumer.browser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.browser.model.entity.Browser;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.ReportManager;

public class BrowserAnalyzer extends AbstractMessageAnalyzer<BrowserReport>
		implements LogEnabled {
	public static final String ID = "browser";

	@Inject(ID)
	private ReportManager<BrowserReport> m_reportManager;
	
	Map<String, String[]> values = new HashMap<String, String[]>();

	public BrowserReport update(BrowserReport report, String[] agentToken) {
		for (String s : agentToken) {
			for (String browser : BrowsersAndOses.BROWSERS) {
				int index;
				index = s.toUpperCase().indexOf(browser.toUpperCase());
				if (index >= 0) {
					String subS = s.substring(index);
					String[] browserAndVersionString = split(subS, "/");
					Browser b = report.findOrCreateDomainDetail("Cat")
							.findOrCreateBrowser(browserAndVersionString[0]);
					b.setCount(b.getCount() + 1);

					if (browserAndVersionString.length >= 2) {
						int i;
						String version = browserAndVersionString[1];
						for (i = 0; (i < version.length())
								&& (version.charAt(i) >= '0'
										&& version.charAt(i) <= '9' || version
										.charAt(i) == '.'); i++)
							;
						version = version.substring(0, i);
						b.findOrCreateBrowserVersion(version).setCount(
								b.findOrCreateBrowserVersion(version)
										.getCount() + 1);
					}
				}
			}
			for (String os : BrowsersAndOses.OSES) {
				int index;
				index = s.toUpperCase().indexOf(os.toUpperCase());
				if (index >= 0) {
					report.findOrCreateDomainDetail("Cat")
					.findOrCreateOs(os).setCount(report.findOrCreateDomainDetail("Cat")
					.findOrCreateOs(os).getCount() + 1);
				}
			}
		}
		return report;
	}

	public String[] splitAgent(String message) {
		String agent = parseValue("Agent", message);
		String[] agentTokens1 = split(agent, "(");
		String[] agentTokens2 = null;
		for (String s : agentTokens1) {
			String[] temp = split(s, ")");
			agentTokens2 = (String[]) ArrayUtils.addAll(agentTokens2, temp);
		}
		String[] agentTokens3 = null;
		for (String s : agentTokens2) {
			String[] temp = split(s, ";");
			agentTokens3 = (String[]) ArrayUtils.addAll(agentTokens3, temp);
		}
		int i;
		for (i = 0; i < agentTokens3.length; i++) {
			agentTokens3[i] = agentTokens3[i].trim();
		}
		return agentTokens3;
	}

	private String[] split(String message, String divisionChar) {

		StringTokenizer tokenizer = new StringTokenizer(message, divisionChar);
		int i = 0;
		String[] string = new String[tokenizer.countTokens()];// 动态的决定数组的长度
		while (tokenizer.hasMoreTokens()) {
			string[i] = new String();
			string[i] = tokenizer.nextToken();
			i++;
		}
		return string;// 返回字符串数组
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
	public void enableLogging(Logger logger) {

	}

	@Override
	public BrowserReport getReport(String domain) {
		return null;
	}

	@Override
	protected void process(MessageTree tree) {
		Message message = tree.getMessage();
		if (message instanceof Transaction) {
			String type = message.getType();

			if ("URL".equals(type)) {
				List<Message> children = ((Transaction) message).getChildren();

				for (Message child : children) {
					String childType = child.getType();
					String childName = child.getName();
					BrowserReport report = m_reportManager.getHourlyReport(
							getStartTime(), "Cat", true);

					if ("URL".equals(childType)
							&& ("URL.Server".equals(childName) || "ClientInfo"
									.equals(childName))) {
						String data = (String) child.getData();

						updateBrowserReport(report, data);
						return;
					}
				}
			}
		}

	}

	private void updateBrowserReport(BrowserReport report, String data) {
		data = parseValue("Agent", data);
		String[] datas;
		if(values.containsKey(data))
			datas = values.get(data);
		else
			datas = splitAgent(data);
		update(report, datas);
	}

	public class BrowserAndVersion {
		private String browser;
		private String browserVersion;

		public String getBrowser() {
			return browser;
		}

		public void setBrowser(String browser) {
			this.browser = browser;
		}

		public String getBrowserVersion() {
			return browserVersion;
		}

		public void setBrowserVersion(String browserVersion) {
			this.browserVersion = browserVersion;
		}
	}

	private static class BrowsersAndOses {
		public static final String OSES[] = { "Windows NT", "Linux",
				"WindowsMobile", "Android", "Mac OS" };
		public static final String BROWSERS[] = { "Chrome", "Maxthon",
				"AppleWebKit", "QQBrowser", "UC Browser", "Safari",
				"LBBROWSER", "QQ Browser", "UCBrowser" };
	}

	@Override
   public void doCheckpoint(boolean atEnd) {
	   
   }
}
