package com.dianping.cat.system.page.abtest;

import com.dianping.cat.system.SystemContext;

public class Context extends SystemContext<Payload> {
	
	private String m_descriptor;

	public String getDescriptor() {
   	return m_descriptor;
   }

	public void setDescriptor(String descriptor) {
   	m_descriptor = descriptor;
   }
}
