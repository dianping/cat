package com.dianping.cat.abtest.spi.groupstrategy;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

/**
 * 
 * GroupStrategy Configuration format: A:50;B:40;..Default:5
 * 
 * Each number stands for traffic percentage of the total traffic.
 * 
 * @author damon.zhu
 * 
 */
public class TrafficGroupStrategy implements ABTestGroupStrategy {

	private final String m_cookie = "abtest-group";

	private AtomicInteger m_visitorIndex;

	@Override
	public void apply(ABTestContext ctx) {
		String config = ctx.getEntity().getGroupStrategyConfiguration();

	}

}
