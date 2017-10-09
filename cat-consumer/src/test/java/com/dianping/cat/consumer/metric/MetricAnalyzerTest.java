package com.dianping.cat.consumer.metric;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.dal.BusinessReport;
import com.dianping.cat.consumer.dal.BusinessReportDao;
import com.dianping.cat.consumer.metric.MetricAnalyzer.ConfigItem;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.task.TaskManager;

public class MetricAnalyzerTest extends ComponentTestCase {
	private long m_timestamp;

	private MetricAnalyzer m_analyzer;

	private String m_domain = "group";

	private final int MINITE = 60 * 1000;

	private MockBusinessReportDao m_businessReportDao;

	private static int m_bucketCount = 0;

	@Before
	public void setUp() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);
		m_analyzer = new TestMetricAnalyzer();
		m_businessReportDao = new MockBusinessReportDao();

		m_analyzer.setBucketManager(new MockBuckerManager());
		m_analyzer.setConfigManager(new MockMetricConfigManager());
		m_analyzer.setTaskManager(new MockTaskManager());
		m_analyzer.setBusinessReportDao(m_businessReportDao);
		m_analyzer.setProductLineConfigManager(new MockProductLineManager());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 60; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		MetricReport report = m_analyzer.getReport(m_domain);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("metric_analyzer.xml"), "utf-8");

		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));

		m_analyzer.storeReports(true);

		Assert.assertEquals(2, m_businessReportDao.m_count);
		Assert.assertEquals(2, m_bucketCount);
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;

		if (i % 3 == 0) {
			t = new DefaultTransaction("URL", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + i * MINITE);
			DefaultEvent event = new DefaultEvent("URL", "ABTest");

			DefaultMetric metric = new DefaultMetric("City", "/beijing");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("S");
			metric.addData("10");

			t.addChild(metric);
			t.addChild(event);
		} else if (i % 3 == 1) {
			t = new DefaultTransaction("Service", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + i * MINITE);
			DefaultEvent event = new DefaultEvent("URL", "ABTest");

			DefaultMetric metric = new DefaultMetric("", "/nanjing");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("S,C");
			metric.addData("10,10");

			t.addChild(metric);
			t.addChild(event);
		} else {
			t = new DefaultTransaction("Metric", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + 1000);
			DefaultMetric metric = new DefaultMetric("", "/shanghai");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("C");
			metric.addData("10");

			t.addChild(metric);

			DefaultMetric durationMetric = new DefaultMetric("", "/shenzhen");

			durationMetric.setTimestamp(m_timestamp + i * MINITE);
			durationMetric.setStatus("T");
			durationMetric.addData("10");

			t.addChild(durationMetric);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		tree.setMessage(t);

		return tree;
	}

	public static class MockMetricConfigManager extends MetricConfigManager {

		private MetricItemConfig m_config = new MetricItemConfig();

		@Override
		public void initialize() throws InitializationException {
		}

		@Override
		public boolean insertMetricItemConfig(MetricItemConfig config) {
			return true;
		}

		@Override
		public boolean insertMetricIfNotExist(String domain, String type, String metricKey, ConfigItem item) {
			return true;
		}

		@Override
		public MetricItemConfig queryMetricItemConfig(String id) {
			return m_config;
		}
	}

	public class TestMetricAnalyzer extends MetricAnalyzer {

		@Override
		protected boolean isLocalMode() {
			return false;
		}
	}

	public class MockBuckerManager implements ReportBucketManager {

		@Override
		public void closeBucket(ReportBucket bucket) {
		}

		@Override
		public ReportBucket getReportBucket(long timestamp, String name, int index) throws IOException {
			return new MockStringBucket();
		}

		@Override
		public void clearOldReports() {
		}

	}

	public class MockBusinessReportDao extends BusinessReportDao {

		public int m_count;

		@Override
		public int insert(BusinessReport proto) throws DalException {
			return m_count++;
		}
	}

	public static class MockStringBucket implements ReportBucket {
		@Override
		public void close() throws IOException {
		}

		@Override
		public String findById(String id) throws IOException {
			return "";
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public Collection<String> getIds() {
			List<String> list = new ArrayList<String>();

			return list;
		}

		@Override
		public void initialize(String name, Date timestamp, int index) throws IOException {
		}

		@Override
		public boolean storeById(String id, String data) throws IOException {
			m_bucketCount++;
			return true;
		}

	}

	public static class MockTaskManager extends TaskManager {
		private Map<Integer, Set<String>> m_results = new HashMap<Integer, Set<String>>();

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		@Override
		protected void createTask(Date period, String ip, String domain, int reportType) throws DalException {
			Set<String> lists = m_results.get(reportType);

			if (lists == null) {
				lists = new HashSet<String>();
				m_results.put(reportType, lists);
			}

			lists.add(sdf.format(period));
		}

		public Map<Integer, Set<String>> getResults() {
			return m_results;
		}
	}

	public static class MockProductLineManager extends ProductLineConfigManager {

		@Override
		public Pair<Boolean, String> insertProductLine(ProductLine line, String[] domains, String title) {
			return new Pair<Boolean, String>(true, null);
		}

		@Override
		public String queryProductLineByDomain(String domain) {
			return domain;
		}

		@Override
		public List<String> queryDomainsByProductLine(String productLine, ProductLineConfig productLineConfig) {
			return new ArrayList<String>();
		}

		@Override
		public Map<String, ProductLine> queryAllProductLines() {
			return new HashMap<String, ProductLine>();
		}

		@Override
		public ProductLineConfig queryProductLine(String id) {
			return null;
		}

		@Override
		public boolean insertIfNotExsit(String product, String domain) {
			return true;
		}
	}

}
