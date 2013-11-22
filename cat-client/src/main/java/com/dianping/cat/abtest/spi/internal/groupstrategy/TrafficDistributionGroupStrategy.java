package com.dianping.cat.abtest.spi.internal.groupstrategy;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class TrafficDistributionGroupStrategy implements ABTestGroupStrategy {

	public static final String ID = "OneVariationStrategy";

	@Inject("Control")
	private int m_percentControl = 50;

	@Inject("Variation-A")
	private int m_percentA = 50;

	private final int m_total = 100;

	private int m_scoreControl = 0;

	private int m_scoreA = 0;

	@Override
	public void apply(ABTestContext ctx) {
		m_scoreControl += m_percentControl;
		m_scoreA += m_percentA;

		if (m_scoreA >= m_scoreControl) {
			ctx.setGroupName("A");
			m_scoreA -= m_total;
		} else {
			ctx.setGroupName(ABTestContext.DEFAULT_GROUP);
			m_scoreControl -= m_total;
		}
	}

	@Override
	public void init() {
		if ((m_percentA + m_percentControl) > 100) {
			m_percentControl = 50;
			m_percentA = 50;
		}
	}

	public void init(int... percents) {
		if (percents.length == 2) {
			if (percents[0] + percents[1] == 100) {
				m_percentControl = percents[0];
				m_percentA = percents[1];
			}
		}
	}
}
