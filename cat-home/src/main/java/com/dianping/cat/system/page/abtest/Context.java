package com.dianping.cat.system.page.abtest;

import com.dianping.cat.system.SystemContext;

public class Context extends SystemContext<Payload> {
	
	private String m_responseJson;

	public String getResponseJson() {
   	return m_responseJson;
   }

	public void setResponseJson(String responseJson) {
   	m_responseJson = responseJson;
   }
}
