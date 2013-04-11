package com.dianping.cat.abtest.spi;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinGroupStrategy implements ABTestGroupStrategy {

	private AtomicInteger m_pvCounter = new AtomicInteger();

	@Override
	public void apply(ABTestContext ctx) {
		System.out.println(m_pvCounter.get());
		if (m_pvCounter.incrementAndGet() % 2 == 0) {
			ctx.setGroupName("A");
		} else {
			ctx.setGroupName("B");
		}
	}
}
