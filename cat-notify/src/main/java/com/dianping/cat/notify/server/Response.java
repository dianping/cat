package com.dianping.cat.notify.server;

import java.util.HashMap;
import java.util.Map;

public class Response {

	private Map<String, Object> params;

	private String outputType;

	private String template;

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Response() {
		params = new HashMap<String, Object>();
	}

	public void assign(String key, Object value) {
		this.params.put(key, value);
	}

	public Map<String, Object> getParams() {
		return params;
	}
}
