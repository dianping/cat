package com.dianping.cat.abtest.spi.internal.conditions;

import com.dianping.cat.abtest.model.entity.Condition;

public interface ABTestCondition {
	public boolean accept(Condition condition);
}
