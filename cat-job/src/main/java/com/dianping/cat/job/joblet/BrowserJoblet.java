package com.dianping.cat.job.joblet;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.job.joblet.BrowserJoblet.Browser;
import com.dianping.cat.job.joblet.BrowserJoblet.BrowserStat;
import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletContext;
import com.dianping.cat.job.spi.joblet.JobletMeta;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.spi.mapreduce.PojoWritable;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

@JobletMeta(name = "browser", description = "Browser analysis", keyClass = Browser.class, valueClass = BrowserStat.class, combine = true, reducerNum = 1)
public class BrowserJoblet extends ContainerHolder implements Joblet<Browser, BrowserStat> {
	private static final String TOKEN = "&Agent=";

	@Inject
	private BrowserOutputter m_outputter;

	private String getUserAgent(Transaction root) {
		List<Message> children = root.getChildren();

		for (Message child : children) {
			if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("ClientInfo")) {
				// URL:ClientInfo &Agent=<ua>
				String data = child.getData().toString();
				int off = data.indexOf(TOKEN);

				if (off >= 0) {
					return data.substring(off + TOKEN.length());
				}

				break;
			}
		}

		return null;
	}

	@Override
	public boolean initialize(JobCmdLine cmdLine) {
		String inputPath = cmdLine.getArg("inputPath", 0, null);
		String outputPath = cmdLine.getArg("outputPath", 1, null);

		if (inputPath != null) {
			cmdLine.setProperty("inputPath", inputPath);
		}

		if (outputPath != null) {
			cmdLine.setProperty("outputPath", outputPath);
		}

		String outputter = cmdLine.getProperty("outputter", null);

		if (outputter != null) {
			m_outputter = lookup(BrowserOutputter.class, outputter);
		}

		return true;
	}

	@Override
	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException {
		MessageTree tree = treeWritable.get();
		Message root = tree.getMessage();

		if (root instanceof Transaction && root.getType().equals("URL")) {
			String userAgent = getUserAgent((Transaction) root);

			if (userAgent != null) {
				Browser browser = new Browser(userAgent);

				context.write(browser, BrowserStat.ONCE);
			}
		}
	}

	@Override
	public void reduce(JobletContext context, Browser browser, Iterable<BrowserStat> stats) throws IOException,
	      InterruptedException {
		BrowserStat all = new BrowserStat();

		for (BrowserStat stat : stats) {
			all.add(stat.getCount());
		}

		if (context.isInCombiner()) {
			context.write(browser, all);
		} else {
			m_outputter.out(context, browser, all);
		}
	}

	@Override
	public void summary() {
		System.out.println(m_outputter);
	}

	/**
	 * <pre>
	 * MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.1)
	 * 
	 * MApi 1.0 (dpscopehd 1.5.1 appstore; iPad 5.1)
	 * 
	 * MApi 1.0 (dpscope 4.9 appstore; iPhone 4.2.1)
	 * 
	 * MApi 1.0 (dpscope 4.6.1 appstore; iPhone 4.2.1)
	 * 
	 * MApi 1.0 (dpscope 4.8.1 ppzhushou; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (dpscope 4.7 appstore; iPhone 5.0.1)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.7.1 dianping Lenovo_A60; Android 2.3.3)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.9.1.193 null SCH-I519; Android 2.3.6)
	 * 
	 * MApi 1.0 (com.dianping.v1 4.7.1 91zs500 HTC_Desire_S; Android 2.3.5)
	 * </pre>
	 */
	public static class Browser extends PojoWritable {
		private static MessageFormat s_iosFormat = new MessageFormat("{0} ({5} {1} {4}; {2} {3})");

		private static MessageFormat s_androidFormat = new MessageFormat("{0} ({5} {1} {4} {6}; {2} {3})");

		private String m_name;

		private String m_version;

		private String m_osType;

		private String m_osVersion;

		private String m_channel;

		private String m_scope;

		private String m_deviceType;

		public Browser() {
		}

		public Browser(String userAgent) {
			parse(userAgent);
		}

		public String getChannel() {
			return m_channel;
		}

		public String getDeviceType() {
			return m_deviceType;
		}

		public String getName() {
			return m_name;
		}

		public String getOsType() {
			return m_osType;
		}

		public String getOsVersion() {
			return m_osVersion;
		}

		public String getScope() {
			return m_scope;
		}

		public String getVersion() {
			return m_version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;

			result = prime * result + ((m_channel == null) ? 0 : m_channel.hashCode());
			result = prime * result + ((m_deviceType == null) ? 0 : m_deviceType.hashCode());
			result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
			result = prime * result + ((m_osType == null) ? 0 : m_osType.hashCode());
			result = prime * result + ((m_osVersion == null) ? 0 : m_osVersion.hashCode());
			result = prime * result + ((m_scope == null) ? 0 : m_scope.hashCode());
			result = prime * result + ((m_version == null) ? 0 : m_version.hashCode());

			return result;
		}

		private void parse(String userAgent) {
			Object[] values = null;

			try {
				if (values == null) {
					values = s_androidFormat.parse(userAgent);
				}
			} catch (ParseException e) {
				// ignore it
			}

			try {
				if (values == null) {
					values = s_iosFormat.parse(userAgent);
				}
			} catch (ParseException e) {
				// ignore it
			}

			if (values == null) {
				m_name = userAgent;
			} else {
				int len = values.length;
				int index = 0;

				m_name = len > index ? (String) values[index++] : null;
				m_version = len > index ? (String) values[index++] : null;
				m_osType = len > index ? (String) values[index++] : null;
				m_osVersion = len > index ? (String) values[index++] : null;
				m_channel = len > index ? (String) values[index++] : null;
				m_scope = len > index ? (String) values[index++] : null;
				m_deviceType = len > index ? (String) values[index++] : null;
			}
		}
	}

	public static interface BrowserOutputter {
		public void out(JobletContext context, Browser browser, BrowserStat all) throws IOException, InterruptedException;
	}

	public static class BrowserStat extends PojoWritable {
		public static final BrowserStat ONCE = new BrowserStat().add(1);

		private int m_count;

		public BrowserStat add(int count) {
			m_count += count;
			return this;
		}

		public int getCount() {
			return m_count;
		}

		@Override
		public int hashCode() {
			return m_count;
		}
	}

	public static class DefaultBrowserOutputter implements BrowserOutputter {
		@Override
		public void out(JobletContext context, Browser browser, BrowserStat all) throws IOException, InterruptedException {
			context.write(browser, all);
		}
	}

	public static class OsTypeAndVersionReporter implements BrowserOutputter, Initializable {
		private Map<String, Map<String, BrowserStat>> m_map = new TreeMap<String, Map<String, BrowserStat>>();

		private Map<String, String> m_osTypeMapping = new HashMap<String, String>();

		private String getCleanVersion(String version) {
			int len = version == null ? 0 : version.length();
			StringBuilder sb = new StringBuilder(len);

			for (int i = 0; i < len; i++) {
				char ch = version.charAt(i);

				if (Character.isDigit(ch) || ch == '.') {
					sb.append(ch);
				} else {
					break;
				}
			}

			return sb.toString();
		}

		@Override
		public void initialize() throws InitializationException {
			m_osTypeMapping.put("iphone", "iPhone");
			m_osTypeMapping.put("ipad", "iPad");
			m_osTypeMapping.put("ipod", "iPod");
			m_osTypeMapping.put("android", "Android");
			m_osTypeMapping.put(null, "Others");
		}

		@Override
		public void out(JobletContext context, Browser browser, BrowserStat stat) throws IOException,
		      InterruptedException {
			String osType = trim(browser.getOsType());
			String osVersion = getCleanVersion(trim(browser.getOsVersion()));
			String type = m_osTypeMapping.get(osType == null ? null : osType.toLowerCase());

			if (type == null) {
				type = m_osTypeMapping.get(null);
			}

			Map<String, BrowserStat> map = m_map.get(type);

			if (map == null) {
				map = new TreeMap<String, BrowserStat>();
				m_map.put(type, map);
			}

			String key = osType + ":" + osVersion;
			BrowserStat s = map.get(key);

			if (s == null) {
				s = new BrowserStat();
				map.put(key, s);
			}

			s.add(stat.getCount());
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(8192);

			sb.append("Group by os type\n");

			for (Map.Entry<String, Map<String, BrowserStat>> e : m_map.entrySet()) {
				String key = e.getKey();
				Map<String, BrowserStat> value = e.getValue();
				int count = 0;

				for (BrowserStat stat : value.values()) {
					count += stat.getCount();
				}

				sb.append(String.format("%-8s %8s\n", key, count));
			}

			sb.append("\n");
			sb.append("Group by os type and version\n");

			for (Map.Entry<String, Map<String, BrowserStat>> e : m_map.entrySet()) {
				String key = e.getKey();
				Map<String, BrowserStat> value = e.getValue();

				for (Map.Entry<String, BrowserStat> s : value.entrySet()) {
					sb.append(String.format("%-8s %-18s %s\n", key, s.getKey(), s.getValue().getCount()));
				}
			}

			return sb.toString();
		}

		private String trim(String str) {
			return str == null ? str : str.trim();
		}
	}
}
