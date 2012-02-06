package com.dianping.cat.report.page.ip;

import junit.framework.Assert;

import org.junit.Test;

public class DisplayModelTest {
	private void check(DisplayModel model, int time, int count, int lastOne, int lastFive, int lastFifteen) {
		model.process(0, time, count);

		Assert.assertEquals("Last one", lastOne, model.getLastOne());
		Assert.assertEquals("Last five", lastFive, model.getLastFive());
		Assert.assertEquals("Last fifteen", lastFifteen, model.getLastFifteen());
	}

	@Test
	public void test() {
		DisplayModel model = new DisplayModel("localhost");

		check(model, 1, 1, 0, 0, 0);
		check(model, 0, 1, 1, 1, 1);
		check(model, -1, 2, 1, 3, 3);
		check(model, -3, 2, 1, 5, 5);
		check(model, -5, 2, 1, 5, 7);
		check(model, -10, 2, 1, 5, 9);
		check(model, -14, 2, 1, 5, 11);

		check(model, -15, 2, 1, 5, 11);
		check(model, 1, 2, 1, 5, 11);
	}
}
