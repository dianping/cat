package com.dianping.cat.abtest.demo.roundrobin;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class RoundRobinGroupStrategy implements ABTestGroupStrategy {
	private AtomicInteger m_pvCounter = new AtomicInteger();

	@Override
	public void apply(ABTestContext ctx) {
		int value = m_pvCounter.incrementAndGet();

		if (value % 3 == 0) {
			ctx.setGroupName("A");
		} else if (value % 3 == 1) {
			ctx.setGroupName("B");
		} else {
			// do nothing
		}
	}

	@Override
   public void init() {
   }
}
