package com.dianping.cat.consumer.failure;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer.Handler;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FailureAnalyzerStoreTest extends ComponentTestCase {
	private String m_domain="middleware";
	
	private String m_host="127.0.0.1";
	@Test
	public void testLookup() throws Exception {
		Handler failure = lookup(Handler.class, "failure-handler");
		Handler longUrl = lookup(Handler.class, "long-url-handler");

		// make sure all handlers could be looked up successfully
		Assert.assertNotNull(failure);
		Assert.assertNotNull(longUrl);
	}

	@Test
	public void testJson() throws Exception {
		long current = 1327470645035L;
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);
		

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory.create("failure", start, duration, m_domain,
		      extraTime);
		int number = 5;

		for (int i = 0; i < number; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("MessageId" + i);
			tree.setThreadId("Thread" + i);
			tree.setDomain(m_domain);
			tree.setHostName(m_host);
			tree.setIpAddress(m_host);
			tree.setMessage(t);
			t.setDuration(3 * 1000);
			t.setTimestamp(start + 1000L * 60 * i);
			analyzer.process(tree);
			analyzer.process(tree);
			analyzer.process(tree);
		}
		
		List<FailureReport> report = analyzer.generate();
		analyzer.store(report);
		
		FailureReport targetReport = analyzer.generateByDomainAndIp(m_domain,m_host);
		String json = new DefaultJsonBuilder().buildJson(targetReport);
		String expected = Files.forIO().readFrom(getResourceFile("failure.json"), "utf-8");

		Assert.assertEquals("Check json content!", expected.replace("\r", ""), json.replace("\r", ""));
	}

	@Test
	public void testStore() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory.create("failure", start, duration, m_domain,
		      extraTime);
		int number = 20;
		for (int i = 0; i < number; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("thread0001");
			tree.setDomain(m_domain);
			tree.setHostName("middleware");
			tree.setIpAddress(m_host);
			tree.setThreadId("Thread" + i%5);
			tree.setMessage(t);
			t.setDuration(3 * 1000);
			t.setTimestamp(start + 1000L * 60 * i);
			analyzer.process(tree);
		}

		List<FailureReport> report = analyzer.generate();
		analyzer.store(report);
		
		FailureReport targetReport = analyzer.generateByDomainAndIp(m_domain,m_host);
		
		String pathname = analyzer.getFailureFilePath(targetReport);
		File storeFile = new File(pathname);
		Assert.assertEquals("Check file is exist!", true, storeFile.exists());
		String realResult = Files.forIO().readFrom(storeFile, "utf-8");
		String exceptedResult = FailureReportStore.getStoreString(targetReport);
		Assert.assertEquals("Check file content!", exceptedResult, realResult);
	}
}
