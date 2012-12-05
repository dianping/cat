package com.dianping.cat.job.sql;

import java.text.DecimalFormat;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SqlJobResultTest {

	@Test
	public void testGetDuration() {
		SqlJobResult sql = new SqlJobResult();

		Assert.assertEquals(0, sql.getDuration(0.1));
		Assert.assertEquals(1, sql.getDuration(1.1));
		Assert.assertEquals(2, sql.getDuration(2));
		Assert.assertEquals(2, sql.getDuration(2.1));
		Assert.assertEquals(4, sql.getDuration(4));
		Assert.assertEquals(4, sql.getDuration(5));
		Assert.assertEquals(4, sql.getDuration(7));
		Assert.assertEquals(8, sql.getDuration(8));
		Assert.assertEquals(8, sql.getDuration(9));
		Assert.assertEquals(8, sql.getDuration(10));
	}

	@Test
	public void testAdd() {
		SqlJobResult sql = new SqlJobResult();

		int totaoMinute = 60;
		double maxDuration = 10;

		for (int minute = 0; minute < totaoMinute; minute++) {
			for (int duration = 1; duration <= maxDuration; duration++) {
				for (int flag = 0; flag < 2; flag++) {
					sql.add(duration, flag, "test", minute);
				}
			}
		}
		sql.toString();

		Assert.assertEquals(1200, sql.getDurations().size());
		Assert.assertEquals(600, sql.getFailureCount());
		Assert.assertEquals(0, sql.getLongTimeCount());
		Assert.assertEquals(1.0, sql.getMin());
		Assert.assertEquals(10.0, sql.getMax());
		Assert.assertEquals(6600.0, sql.getSum());
		Assert.assertEquals(46200.0, sql.getSum2());

		DecimalFormat df = new DecimalFormat("#.##");
		Assert.assertEquals("10", df.format(sql.getPercent95Line()));

		Map<Integer, Integer> durationDistribution = sql.getDurationDistribution();
		for (int i = 0; i <= 65536;) {
			if (i == 0) {
				Assert.assertEquals(0, (int) durationDistribution.get(0));
			} else if (i == 1) {
				Assert.assertEquals(2 * 60, (int) durationDistribution.get(1));
			} else if (i == 2) {
				Assert.assertEquals(2 * 2 * 60, (int) durationDistribution.get(2));
			} else if (i == 4) {
				Assert.assertEquals(2 * 4 * 60, (int) durationDistribution.get(4));
			} else if (i == 8) {
				Assert.assertEquals(2 * 3 * 60, (int) durationDistribution.get(8));
			} else {
				Assert.assertEquals(0, (int) durationDistribution.get(i));
			}
			if (i == 0) {
				i++;
			} else {
				i = i * 2;
			}
		}

		Map<Integer, Integer> hitsOverTime = sql.getHitsOverTime();
		for (int i = 0; i < 60; i = i + 5) {
			Assert.assertEquals(100, (int) hitsOverTime.get(i));
		}
		Assert.assertEquals(0, (int) hitsOverTime.get(60));

		Map<Integer, Integer> failureOverTime = sql.getFailureOverTime();
		for (int i = 0; i < 60; i = i + 5) {
			Assert.assertEquals(50, (int) failureOverTime.get(i));
		}
		Assert.assertEquals(0, (int) hitsOverTime.get(60));

		Map<Integer, Double> durationOverTime = sql.getDurationOverTime();
		for (int i = 0; i < 60; i = i + 5) {
			Assert.assertEquals(5.5, (double) durationOverTime.get(i));
		}
		Assert.assertEquals(0, (int) hitsOverTime.get(60));
		
		double sum = 0;
		for(int i=0;i<60;i=i+5){
			sum = sum +durationOverTime.get(i)*hitsOverTime.get(i);
		}
		Assert.assertEquals(6600.0, sum);
	}
}
