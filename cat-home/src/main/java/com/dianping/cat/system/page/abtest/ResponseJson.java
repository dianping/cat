package com.dianping.cat.system.page.abtest;

public class ResponseJson {
	private int m_code;

	private String m_msg;

	public ResponseJson(int code, String msg) {
		m_code = code;
		m_msg = msg;
	}

	public int getCode() {
		return m_code;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}
}