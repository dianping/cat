package com.dianping.cat.system.page.abtest;

import java.util.List;

import com.dianping.cat.system.SystemContext;
import com.dianping.cat.system.page.abtest.advisor.ABTestAdvice;

public class Context extends SystemContext<Payload> {

	private String m_responseJson;

	private List<ABTestAdvice> m_advices;

	public String getResponseJson() {
		return m_responseJson;
	}

	public void setResponseJson(String responseJson) {
		m_responseJson = responseJson;
	}

	public void setAdvice(List<ABTestAdvice> advices) {
		m_advices = advices;
	}

	public List<ABTestAdvice> getAdvice() {
		return m_advices;
	}
}
