package com.dianping.cat.abtest.conditions;

import javax.servlet.http.HttpServletRequest;

public interface ABTestCondition {
	public boolean accept(HttpServletRequest request);
}
