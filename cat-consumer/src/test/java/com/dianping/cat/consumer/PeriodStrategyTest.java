package com.dianping.cat.consumer;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.RealtimeConsumer.PeriodStrategy;

public class PeriodStrategyTest {
	@Test
	public void test1() {
		RealtimeConsumer consumer = new RealtimeConsumer();
		PeriodStrategy strategy = consumer.new PeriodStrategy(60, 5, 3);

		Assert.assertEquals(0, strategy.next(4));
		Assert.assertEquals(0, strategy.next(5));
		Assert.assertEquals(0, strategy.next(6));
		Assert.assertEquals(0, strategy.next(56));
		Assert.assertEquals(60, strategy.next(57));
		Assert.assertEquals(0, strategy.next(58));
		Assert.assertEquals(0, strategy.next(64));
		Assert.assertEquals(-0, strategy.next(65));
		Assert.assertEquals(120, strategy.next(117));
		Assert.assertEquals(-60, strategy.next(125));
		Assert.assertEquals(180, strategy.next(177));
		Assert.assertEquals(-120, strategy.next(185));
		Assert.assertEquals(0, strategy.next(236));
		Assert.assertEquals(240, strategy.next(237));
		Assert.assertEquals(-180, strategy.next(245));
		Assert.assertEquals(360, strategy.next(400));
		Assert.assertEquals(-240, strategy.next(401));
		Assert.assertEquals(420, strategy.next(417));
		Assert.assertEquals(-360, strategy.next(425));
		Assert.assertEquals(1380, strategy.next(1400));
		Assert.assertEquals(-420, strategy.next(1401));
	}

	@Test
	public void test2() {
		RealtimeConsumer consumer = new RealtimeConsumer();
		PeriodStrategy strategy = consumer.new PeriodStrategy(60, 5, 3);

		Assert.assertEquals(60, strategy.next(104));
		Assert.assertEquals(0, strategy.next(105));
		Assert.assertEquals(120, strategy.next(117));
		Assert.assertEquals(-60, strategy.next(125));
		Assert.assertEquals(180, strategy.next(177));
		Assert.assertEquals(-120, strategy.next(185));
	}
	
}
