package com.dianping.cat.report.page.statistics;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.home.bug.entity.ExceptionItem;

public class ErrorStatis {
	private String m_productLine;

	private String m_department;

	private Map<String, ExceptionItem> m_bugs = new HashMap<String, ExceptionItem>();

	private Map<String, ExceptionItem> m_exceptions = new HashMap<String, ExceptionItem>();

	public Map<String, ExceptionItem> getBugs() {
		return m_bugs;
	}

	public String getDepartment() {
		return m_department;
	}

	public Map<String, ExceptionItem> getExceptions() {
		return m_exceptions;
	}

	public String getProductLine() {
		return m_productLine;
	}

	public void setBugs(Map<String, ExceptionItem> bugs) {
		m_bugs = bugs;
	}

	public void setDepartment(String department) {
		m_department = department;
	}

	public void setExceptions(Map<String, ExceptionItem> exceptions) {
		m_exceptions = exceptions;
	}

	public void setProductLine(String productLine) {
		m_productLine = productLine;
	}
}