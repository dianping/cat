package com.dianping.cat.matrix.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.matrix.model.entity.MatrixReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class MatrixAnalyzerTest extends ComponentTestCase {
	private long m_timestamp;

	private MatrixAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void before() throws Exception {
		defineComponent(ReportManager.class, MatrixAnalyzer.ID, MockMatrixReportManager.class) //
		      .req(ReportDelegate.class, MatrixAnalyzer.ID);
		defineComponent(ReportDelegate.class, MatrixAnalyzer.ID, MatrixDelegate.class);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);
		m_analyzer = (MatrixAnalyzer) lookup(MessageAnalyzer.class, MatrixAnalyzer.ID);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 100; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		MatrixReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;
		DefaultTransaction event;

		if (i % 3 == 0) {
			t = new DefaultTransaction("URL", "Cat-Test-Call", null);
			event = new DefaultTransaction("Call", "192.168.1.0:3000:class:method1", null);
		} else if (i % 3 == 1) {
			t = new DefaultTransaction("PigeonService", "Cat-Test-Service", null);
			event = new DefaultTransaction("SQL", "192.168.1.2:3000:class:method2", null);
		} else {
			t = new DefaultTransaction("Service", "Cat-Test-Service", null);
			event = new DefaultTransaction("Cache.CatTest", "192.168.1.2:3000:class:method2", null);
		}

		event.setTimestamp(m_timestamp + 5 * 60 * 1000);
		event.setDurationInMillis(i);
		event.setStatus(Message.SUCCESS);
		t.addChild(event);

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}

	public static class MockMatrixReportManager extends MockReportManager<MatrixReport> {
		private MatrixReport m_report;

		@Inject
		private ReportDelegate<MatrixReport> m_delegate;

		@Override
		public MatrixReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (MatrixReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
      public void destory() {
      }

		@Override
      public Map<String, MatrixReport> loadHourlyReports(long startTime, StoragePolicy policy, int index) {
	      return null;
      }

		@Override
      public void storeHourlyReports(long startTime, StoragePolicy policy, int index) {
      }
	}
}
