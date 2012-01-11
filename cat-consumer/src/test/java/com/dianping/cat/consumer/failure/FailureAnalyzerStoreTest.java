package com.dianping.cat.consumer.failure;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.transform.DefaultJsonBuilder;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FailureAnalyzerStoreTest extends ComponentTestCase {

	@Test 
	public void testJson()throws Exception{
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
				.create("failure", start, duration, "domain1", extraTime);
		int number = 5;
		for (int i = 0; i < number; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("MessageId"+ i);
			tree.setThreadId("Thread" + i);
			tree.setDomain("middleware");
			tree.setHostName("middleware");
			tree.setMessage(t);
			tree.setIpAddress("192.168.8."+i%4);
			t.setDuration(3 * 1000);
			t.setTimestamp(start + 1000 * 60 * i);
			analyzer.process(tree);
			analyzer.process(tree);
			analyzer.process(tree);
		}

		FailureReport report = analyzer.generate();
		analyzer.store(report);

		DefaultJsonBuilder jsonBuilder = new DefaultJsonBuilder();
		jsonBuilder.visitFailureReport(report);
		
		/*String realResult = jsonBuilder.getString();

		Gson gson = new Gson();
		String exceptedResult = gson.toJson(report,FailureReport.class);
		
		Assert.assertEquals("Check json content!", exceptedResult, realResult);
		 */	
		}
	
	
	@Test
	public void testStore() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
				.create("failure", start, duration, "domain1", extraTime);
		int number = 20;
		for (int i = 0; i < number; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("thread0001");
			tree.setDomain("middleware");
			tree.setHostName("middleware");
			tree.setMessage(t);
			t.setDuration(3 * 1000);
			t.setTimestamp(start + 1000 * 60 * i);
			analyzer.process(tree);
		}

		FailureReport report = analyzer.generate();
		analyzer.store(report);

		String parentPath = analyzer.getReportPath();
		String pathname = parentPath + analyzer.getFailureFileName(report);
		File storeFile = new File(pathname + ".html");
		Assert.assertEquals("Check file is exist!", true, storeFile.exists());
		String realResult = Files.forIO().readFrom(storeFile, "utf-8");
		String exceptedResult = FailureReportStore.getStoreString(report);
		Assert.assertEquals("Check file content!", exceptedResult, realResult);
	}
}
