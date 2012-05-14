package com.dianping.cat.job.joblet;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.joblet.BrowserJoblet.Browser;
import com.dianping.cat.job.joblet.BrowserJoblet.BrowserStat;
import com.dianping.cat.job.joblet.BrowserJoblet.BrowserOutputter;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletContext;
import com.dianping.cat.job.spi.joblet.JobletRunner;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BrowserJobletTest extends ComponentTestCase {
	private Message buildMockMessage(String userAgent) {
		DefaultTransaction t = new DefaultTransaction("URL", "test", null);
		DefaultEvent e = new DefaultEvent("URL", "ClientInfo");

		e.addData("RemoteIp=...&Agent=" + userAgent);
		e.setStatus(Message.SUCCESS);
		e.complete();
		t.addChild(e);
		t.setStatus(Message.SUCCESS);
		t.complete();

		return t;
	}

	private void checkMapper(String userAgent, final String expectedOutputKey) throws Exception {
		BrowserJoblet joblet = (BrowserJoblet) lookup(Joblet.class, "browser");
		MessageTree tree = new DefaultMessageTree();
		MessageTreeWritable treeWritable = new MessageTreeWritable(tree);
		final SoftReference<Boolean> flag = new SoftReference<Boolean>(true);

		tree.setMessage(buildMockMessage(userAgent));

		joblet.map(new JobletContext() {
			@Override
			public void write(Object key, Object value) throws IOException, InterruptedException {
				flag.clear();
				Assert.assertEquals(expectedOutputKey, key.toString());
			}
		}, treeWritable);

		Assert.assertNull("Mapper has not been triggered.", flag.get());

		release(joblet);
	}

	@Test
	@Ignore
	public void testJoblet() throws Exception {
		JobletRunner runner = lookup(JobletRunner.class);
		MockOutputter listener = (MockOutputter) lookup(BrowserOutputter.class);
		int exitCode = runner.run("browser", "target/12", "-Dreducers=1");

		Assert.assertEquals(0, exitCode);

		listener.show();
	}

	@Test
	public void testMapper() throws Exception {
		checkMapper("MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.1)", "MApi 1.0|4.8.1|iPhone|5.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscopehd 1.5.1 appstore; iPad 5.1)", "MApi 1.0|1.5.1|iPad|5.1|appstore|dpscopehd|");
		checkMapper("MApi 1.0 (dpscope 4.9 appstore; iPhone 4.2.1)", "MApi 1.0|4.9|iPhone|4.2.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.6.1 appstore; iPhone 4.2.1)", "MApi 1.0|4.6.1|iPhone|4.2.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.8.1 ppzhushou; iPhone 5.0.1)", "MApi 1.0|4.8.1|iPhone|5.0.1|ppzhushou|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.8.1 appstore; iPhone 5.0.1)", "MApi 1.0|4.8.1|iPhone|5.0.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (dpscope 4.7 appstore; iPhone 5.0.1)", "MApi 1.0|4.7|iPhone|5.0.1|appstore|dpscope|");
		checkMapper("MApi 1.0 (com.dianping.v1 4.7.1 dianping Lenovo_A60; Android 2.3.3)", "MApi 1.0|4.7.1|Android|2.3.3|dianping|com.dianping.v1|Lenovo_A60");
		checkMapper("MApi 1.0 (com.dianping.v1 4.9.1.193 null SCH-I519; Android 2.3.6)", "MApi 1.0|4.9.1.193|Android|2.3.6|null|com.dianping.v1|SCH-I519");
		checkMapper("MApi 1.0 (com.dianping.v1 4.7.1 91zs500 HTC_Desire_S; Android 2.3.5)", "MApi 1.0|4.7.1|Android|2.3.5|91zs500|com.dianping.v1|HTC_Desire_S");
	}

	public static class MockOutputter implements BrowserOutputter {
		private Map<String, BrowserStat> m_groupByOsType = new TreeMap<String, BrowserStat>();

		private Map<String, BrowserStat> m_groupByIosAndOsVersion = new TreeMap<String, BrowserStat>();

		private Map<String, BrowserStat> m_groupByAndriodAndOsVersion = new TreeMap<String, BrowserStat>();

		private void addTo(Map<String, BrowserStat> map, String key, BrowserStat other) {
			BrowserStat stat = map.get(key);

			if (stat == null) {
				stat = new BrowserStat();
				map.put(key, stat);
			}

			stat.add(other.getCount());
		}

		public Map<String, BrowserStat> getGroupByAndriodAndOsVersion() {
			return m_groupByAndriodAndOsVersion;
		}

		public Map<String, BrowserStat> getGroupByIosAndOsVersion() {
			return m_groupByIosAndOsVersion;
		}

		public Map<String, BrowserStat> getGroupByOsType() {
			return m_groupByOsType;
		}

		@Override
		public void out(JobletContext context, Browser browser, BrowserStat stat) {
			String osType = browser.getOsType();
			String osVersion = browser.getOsVersion();

			if ("iphone".equalsIgnoreCase(osType)) {
				addTo(m_groupByOsType, "iPhone", stat);
				addTo(m_groupByIosAndOsVersion, osType + ":" + osVersion, stat);
			} else if ("ipad".equalsIgnoreCase(osType)) {
				addTo(m_groupByOsType, "iPad", stat);
				addTo(m_groupByIosAndOsVersion, osType + ":" + osVersion, stat);
			} else if ("android".equalsIgnoreCase(osType)) {
				addTo(m_groupByOsType, "Android", stat);

				int pos1 = osVersion.indexOf('-');
				int pos2 = osVersion.indexOf('_');
				int pos3 = osVersion.indexOf(' ');
				int pos = osVersion.length();

				if (pos1 > 0) {
					pos = Math.min(pos, pos1);
				}

				if (pos2 > 0) {
					pos = Math.min(pos, pos2);
				}

				if (pos3 > 0) {
					pos = Math.min(pos, pos3);
				}

				addTo(m_groupByAndriodAndOsVersion, osType + ":" + osVersion.substring(0, pos), stat);
			} else {
				addTo(m_groupByOsType, "Other", stat);
			}
		}

		public void show() {
			showTable("OS Type", m_groupByOsType);
			showTable("iOS and Version", m_groupByIosAndOsVersion);
			showTable("Android and Version", m_groupByAndriodAndOsVersion);
		}

		private void showRow(String name, String count, int nameMaxLen, int countMaxLen) {
			StringBuilder sb = new StringBuilder(nameMaxLen + countMaxLen + 1);

			sb.append(name);

			for (int i = name.length(); i < nameMaxLen; i++) {
				sb.append(' ');
			}

			sb.append(' ');

			for (int i = count.length(); i < countMaxLen; i++) {
				sb.append(' ');
			}

			sb.append(count);

			System.out.println(sb);
		}

		private void showTable(String title, Map<String, BrowserStat> map) {
			int len = map.size();
			String[] names = new String[len];
			int[] counts = new int[len];
			int maxLen = title.length();
			int maxCount = 0;
			int index = 0;

			for (Map.Entry<String, BrowserStat> e : map.entrySet()) {
				String name = e.getKey();
				int count = e.getValue().getCount();

				if (name.length() > maxLen) {
					maxLen = name.length();
				}

				if (count > maxCount) {
					maxCount = count;
				}

				names[index] = name;
				counts[index] = count;
				index++;
			}

			showTable(title, "Count", names, counts, maxLen, Math.max(5, String.valueOf(maxCount).length()));
		}

		private void showTable(String nameTitle, String countTitle, String[] names, int[] counts, int nameMaxLen,
		      int countMaxLen) {
			showRow(nameTitle, countTitle, nameMaxLen, countMaxLen);

			for (int i = 0; i < names.length; i++) {
				showRow(names[i], String.valueOf(counts[i]), nameMaxLen, countMaxLen);
			}
		}
	}
}
