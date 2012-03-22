package com.dianping.cat.job.sql;

import java.text.DecimalFormat;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.sql.database.SqlReportJobRecord;

@RunWith(JUnit4.class)
public class SqlRecordTest {

	@Test
	public void test() {
		DecimalFormat df = new DecimalFormat("#.##");

		Assert.assertEquals("1.23", df.format(1.234567));
	}

	@Test
	public void test2() {
		String text = "domain1	SQLStatement-Internal9	insert into mysql where is='sfsdf'	"+"" +
				"500	500	500	100	199	74750	11591750	147	www.sina.com	m_durationDistribution	m_hitsOverTime	m_durationOverTime	m_failureOverTime";
		SqlReportJobRecord record = new SqlReportJobRecord("20120309/11", text);

		Assert.assertEquals("domain1", record.getDomain());
		Assert.assertEquals("SQLStatement-Internal9", record.getName());
		Assert.assertEquals("insert into mysql where is='sfsdf'", record.getStatement());
		Assert.assertEquals(500, record.getTotalCount());
		Assert.assertEquals(500, record.getFailureCount());
		Assert.assertEquals(500, record.getLongCount());
		Assert.assertEquals(100.0, record.getMin());
		Assert.assertEquals(199.0, record.getMax());
		Assert.assertEquals(74750.0, record.getSum());
		Assert.assertEquals(11591750.0, record.getSum2());
		Assert.assertEquals(147.0, record.getAvg2());
		Assert.assertEquals("www.sina.com", record.getSampleLink());
		Assert.assertEquals("m_durationDistribution", record.getDurationDistribution());
		Assert.assertEquals("m_hitsOverTime", record.getHitsOverTime());
		Assert.assertEquals("m_durationOverTime", record.getDurationOverTime());
		Assert.assertEquals("m_failureOverTime", record.getFailureOverTime());
	}
}
