package com.dianping.cat.abtest.spi.internal.groupstrategy;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContext;

public class TrafficDistributionGroupStrategyTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		testApply(50, 50);

		testApply(40, 60);

		testApply(55, 45);
	}

	public void testApply(int expectDefault, int expectA) throws Exception {
		TrafficDistributionGroupStrategy strategy = (TrafficDistributionGroupStrategy) lookup(ABTestGroupStrategy.class,
		      TrafficDistributionGroupStrategy.ID);
		ABTestContext ctx = new DefaultABTestContext(new ABTestEntity());

		strategy.init(expectDefault, expectA);

		int countDefault = 0;
		int countA = 0;

		for (int i = 0; i < 100; i++) {
			strategy.apply(ctx);
			if (ctx.getGroupName().equals(ABTestContext.DEFAULT_GROUP)) {
				countDefault++;
			} else {
				countA++;
			}
		}

		Assert.assertEquals(expectDefault, countDefault);
		Assert.assertEquals(expectA, countA);
	}
}
