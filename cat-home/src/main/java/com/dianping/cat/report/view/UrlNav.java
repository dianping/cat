package com.dianping.cat.report.view;

public enum UrlNav {
	FIRST("-1d", -24), SECOND("-2h", -2), THIRD("-1h", -1), FOUR("+1h", 1), FIVE("+2h", 2), SIX("+1d", 24);

	private UrlNav(String text, int method) {
		m_text = text;
		m_method = method;
	}

	private String m_text;

	private int m_method;

	public String getText() {
		return m_text;
	}

	public int getMethod() {
		return m_method;
	}
}
