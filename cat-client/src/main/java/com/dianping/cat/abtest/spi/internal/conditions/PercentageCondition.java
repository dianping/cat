package com.dianping.cat.abtest.spi.internal.conditions;

import com.dianping.cat.abtest.model.entity.Condition;

public class PercentageCondition extends AbstractABTestCondition implements ABTestCondition {
	public static final String ID = "percent";

	@Override
	public boolean accept(Condition condition) {

		return true;
	}
}
