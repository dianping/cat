package com.dianping.cat.report.page.monitor;

import com.google.gson.annotations.SerializedName;

public class HttpStatus {

	public static final int SUCCESS = 0;

	public static final int FAIL = -1;

	@SerializedName("statusCode")
	private String m_statusCode;

	@SerializedName("errorMsg")
	private String m_errorMsg;

	public String getErrorMsg() {
		return m_errorMsg;
	}

	public String getStatusCode() {
		return m_statusCode;
	}

	public void setErrorMsg(String errorMsg) {
		m_errorMsg = errorMsg;
	}

	public void setStatusCode(String statusCode) {
		m_statusCode = statusCode;
	}

}
