package com.dianping.cat.notify.server;

import javax.servlet.http.HttpServletRequest;

public class Request {

	String processorKey;

	HttpServletRequest req = null;

	public Request(HttpServletRequest req) {
		this.req = req;
	}

	public String getParameter(String key) {
		if (null == this.req) {
			return null;
		}
		return this.req.getParameter(key);
	}

	public String getProcessorKey() {
		return processorKey;
	}

	public void setProcessorKey(String processorKey) {
		this.processorKey = processorKey;
	}

}
