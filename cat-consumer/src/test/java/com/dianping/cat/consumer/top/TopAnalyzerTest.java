package com.dianping.cat.consumer.top;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.problem.Configurator.MockProblemReportManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzerTest;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.Configurator.MockTransactionReportManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

public class TopAnalyzerTest extends ComponentTestCase {

	private TopAnalyzer m_analyzer;

	public void rebuildTransactionReport(TransactionReport report) {
		int i = 0;
		for (Machine machine : report.getMachines().values()) {
			for (TransactionType type : machine.getTypes().values()) {
				Range2 map = type.findOrCreateRange2(i);

				map.setAvg(i + 1);
				map.setCount(i);
				map.setFails(1);
				map.setSum(i * 10);
				map.setValue(2);
				i++;
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();

		try {
			TransactionAnalyzer transactionAnalyzer = (TransactionAnalyzer) lookup(MessageAnalyzer.class,
			      TransactionAnalyzer.ID);
			TransactionDelegate transactionDelegate = (TransactionDelegate) lookup(ReportDelegate.class, "transaction");
			MockTransactionReportManager transactionManager = (MockTransactionReportManager) lookup(ReportManager.class,
			      "transaction");

			String xml = Files.forIO().readFrom(TransactionAnalyzerTest.class.getResourceAsStream("transaction_real.xml"),
			      "utf-8");
			TransactionReport transactionReport = transactionDelegate.parseXml(xml);
			rebuildTransactionReport(transactionReport);
			transactionManager.setReport(transactionReport);

			ProblemAnalyzer problemAnalyzer = (ProblemAnalyzer) lookup(MessageAnalyzer.class, ProblemAnalyzer.ID);
			ProblemDelegate problemDelegate = (ProblemDelegate) lookup(ReportDelegate.class, "problem");
			MockProblemReportManager problemManager = (MockProblemReportManager) lookup(ReportManager.class, "problem");
			xml = Files.forIO().readFrom(ProblemAnalyzerTest.class.getResourceAsStream("problem-report.xml"), "utf-8");
			ProblemReport problemReport = problemDelegate.parseXml(xml);
			problemManager.setReport(problemReport);

			m_analyzer = (TopAnalyzer) lookup(MessageAnalyzer.class, TopAnalyzer.ID);

			m_analyzer.setTransactionAnalyzer(transactionAnalyzer);
			m_analyzer.setProblemAnalyzer(problemAnalyzer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		TopReport report = m_analyzer.getReport("cat");

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("top_analyzer_test.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

}
