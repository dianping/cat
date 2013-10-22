package com.dianping.cat.system.page.abtest.conditions;

import java.util.Map;

import com.dianping.cat.abtest.model.entity.Condition;

public interface ScriptProvider {
	public static final String m_fileName = "trafficFilter.ftl";

	public Map<Integer, Object> options();

	public String getScript(Condition condition);
}
