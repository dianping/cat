package com.dianping.cat.message.consumer.model.failure;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.message.consumer.impl.AnalyzerFactory;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FailureAnalyzerStoreTest extends ComponentTestCase {

	@Test
	public void testStore() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
				.create("failure", start, duration, "domain1", extraTime);

		DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
		MessageTree tree = new DefaultMessageTree();
		tree.setMessageId("thread0001");
		tree.setDomain("middleware");
		tree.setHostName("middleware");
		tree.setMessage(t);
		t.setDuration(3 * 1000);
		t.setTimestamp(start + 1000 * 60);
		analyzer.process(tree);

		FailureReport report = analyzer.generate();
		analyzer.store(report);
		
		String parentPath = analyzer.getReportPath();
		String pathname = parentPath+analyzer.getFailureFileName(report);
		File storeFile = new File(pathname);
		Assert.assertEquals("Check file is exist!",true, storeFile.exists());
		String realResult=Files.forIO().readFrom(storeFile, "utf-8");
		Assert.assertEquals("Check file content!", report.toString(),realResult);
	}
}
