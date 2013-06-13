package com.dianping.cat.report.analyzer;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.task.transaction.TransactionReportBuilder;

@RunWith(JUnit4.class)
public class TransactionReportBuilderTest extends ComponentTestCase {

	@Inject
	private TransactionReportBuilder m_builder;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_builder = lookup(TransactionReportBuilder.class);
	}

	@Test
	public void test() {
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 10, 3, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date reportPeriod = cal.getTime();
		
		System.out.println(reportPeriod);
		m_builder.buildWeeklyReport("transaction", "Cat", reportPeriod);
	}

}
