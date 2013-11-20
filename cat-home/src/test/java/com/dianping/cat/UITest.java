package com.dianping.cat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Urls;
import org.unidal.helper.Files;

public class UITest {

	private String m_date;

	private String m_local_host = "localhost:2281";

	private String m_qa_host = "cat.qa.dianpingoa.com";

	private List<Item> m_items = new ArrayList<Item>();

	@Before
	public void setUp() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR, -3);

		m_date = new SimpleDateFormat("yyyyMMddHH").format(cal.getTime());
		m_items.add(new Item("http://%s/cat/r/home?domain=Cat&ip=&date=%s&reportType=&op=view", "Home"));
		m_items.add(new Item("http://%s/cat/r/metric?domain=Cat&ip=&date=%s&reportType=&op=view", "Metric"));
		m_items.add(new Item("http://%s/cat/r/t?domain=Cat&ip=&date=%s&reportType=&op=view", "Transaction"));
		m_items.add(new Item("http://%s/cat/r/e?domain=Cat&ip=&date=%s&reportType=&op=view", "Event"));
		m_items.add(new Item("http://%s/cat/r/p?domain=Cat&ip=&date=%s&reportType=&op=view", "Problem"));
		m_items.add(new Item("http://%s/cat/r/h?domain=Cat&ip=&date=%s&reportType=&op=view", "Heartbeat"));
		m_items.add(new Item("http://%s/cat/r/cross?domain=GroupService&ip=&date=%s&reportType=&op=view", "Cross"));
		m_items.add(new Item("http://%s/cat/r/cache?domain=GroupService&ip=&date=%s&reportType=&op=view", "Cache"));
		m_items.add(new Item("http://%s/cat/r/sql?domain=GroupService&ip=&date=%s&reportType=&op=view", "Sql"));
		m_items.add(new Item("http://%s/cat/r/matrix?domain=GroupService&ip=&date=%s&reportType=&op=view", "Matrix"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=view", "Bug"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=service", "Service"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=utilization",
		      "Utilization"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=heavy", "Heavy"));

		m_items.add(new Item("http://%s/cat/r/state?domain=Cat&ip=&date=%s&reportType=&op=view", "State"));
		m_items.add(new Item("http://%s/cat/r/state?domain=GroupService&ip=&date=%s&reportType=&op=view", "State"));
		m_items.add(new Item("http://%s/cat/r/t?domain=Cat&ip=&date=%s&reportType=&op=history", "TransactionHistory"));
		m_items.add(new Item("http://%s/cat/r/e?domain=Cat&ip=&date=%s&reportType=&op=history", "EventHistory"));
		m_items.add(new Item("http://%s/cat/r/p?domain=Cat&ip=&date=%s&reportType=&op=history", "ProblemHistory"));
		m_items.add(new Item("http://%s/cat/r/h?domain=Cat&ip=&date=%s&reportType=&op=history", "HeartbeatHistory"));
		m_items.add(new Item("http://%s/cat/r/cross?domain=GroupService&ip=&date=%s&reportType=&op=history",
		      "CrossHistory"));
		m_items.add(new Item("http://%s/cat/r/cache?domain=GroupService&ip=&date=%s&reportType=&op=history",
		      "CacheHistory"));
		m_items.add(new Item("http://%s/cat/r/sql?domain=GroupService&ip=&date=%s&reportType=&op=history", "SqlHistory"));
		m_items.add(new Item("http://%s/cat/r/matrix?domain=GroupService&ip=&date=%s&reportType=&op=history",
		      "MatrixHistory"));
		m_items.add(new Item("http://%s/cat/r/state?domain=GroupService&ip=&date=%s&reportType=&op=history",
		      "StateHistory"));
		m_items
		      .add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=historyBug", "BugHistory"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=historyService",
		      "ServiceHistory"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=historyUtilization",
		      "UtilizationHistory"));
		m_items.add(new Item("http://%s/cat/r/statistics?domain=Cat&ip=&date=%s&reportType=&op=historyHeavy",
		      "HeavyHistory"));
	}

	@Test
	public void test() {
		for (Item item : m_items) {
			try {
				compare(item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void compare(Item item) throws Exception {
		String url = item.getUrl();
		String localUrl = String.format(url, m_local_host, m_date);
		String qaUrl = String.format(url, m_qa_host, m_date);
		String localContent = Files.forIO().readFrom(Urls.forIO().connectTimeout(5000).openStream(localUrl), "utf-8");
		String qaContent = Files.forIO().readFrom(Urls.forIO().connectTimeout(5000).openStream(qaUrl), "utf-8");
		String localTrim = localContent.replaceAll("\\s*", "");
		String qaTrim = qaContent.replaceAll("\\s*", "");

		if (!localTrim.equals(qaTrim)) {
			System.err.println(item.getTitle() + " Fail！");
		}
	}

	public static class Item {

		private String m_url;

		private String m_title;

		public Item(String url, String title) {
			m_url = url;
			m_title = title;
		}

		public String getUrl() {
			return m_url;
		}

		public String getTitle() {
			return m_title;
		}
	}

}
