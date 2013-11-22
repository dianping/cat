package com.dianping.cat.analysis;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.Configurator.MockAnalyzer1;
import com.dianping.cat.analysis.Configurator.MockAnalyzer2;
import com.dianping.cat.analysis.Configurator.MockAnalyzer3;

public class DefaultMessageAnalyzerManagerTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		DefaultMessageAnalyzerManager manager = (DefaultMessageAnalyzerManager) lookup(MessageAnalyzerManager.class);
		Assert.assertEquals(3, manager.getAnalyzerNames().size());

		long hour = 3600 * 1000L;
		long time = System.currentTimeMillis();
		long current = time - time % hour;
		long last = time - hour;
		long lastTwo = time - 2 * hour;

		MockAnalyzer1 analyzer1 = (MockAnalyzer1) manager.getAnalyzer("mock1", lastTwo);
		MockAnalyzer2 analyzer2 = (MockAnalyzer2) manager.getAnalyzer("mock2", last);
		MockAnalyzer3 analyzer3 = (MockAnalyzer3) manager.getAnalyzer("state", current);
		
		Assert.assertEquals(1, analyzer1.m_count);
		Assert.assertEquals(2, analyzer2.m_count);
		Assert.assertEquals(3, analyzer3.m_count);
	}
}
