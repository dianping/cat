package com.dianping.cat.abtest.spi.internal.conditions;

import javax.servlet.http.HttpServletRequest;

public interface ABTestCondition {
	public boolean accept(HttpServletRequest request);
}
