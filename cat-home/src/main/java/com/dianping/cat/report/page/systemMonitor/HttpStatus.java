package com.dianping.cat.report.page.systemMonitor;

import com.google.gson.annotations.SerializedName;

public class HttpStatus {
	
	@SerializedName("statusCode")
	private String m_statusCode;

	@SerializedName("errorMsg")
	private String m_errorMsg;

	public String getStatusCode() {
		return m_statusCode;
	}

	public void setStatusCode(String statusCode) {
		m_statusCode = statusCode;
	}

	public String getErrorMsg() {
		return m_errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		m_errorMsg = errorMsg;
	}

}
