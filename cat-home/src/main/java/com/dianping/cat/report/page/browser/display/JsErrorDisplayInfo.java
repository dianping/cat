package com.dianping.cat.report.page.browser.display;

import java.util.List;
import java.util.Map;

import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.graph.PieChart;

public class JsErrorDisplayInfo {

	private List<String> m_levels;

	private List<String> m_modules;

	private int m_totalCount;

	private List<LogMsg> m_errors;

	private Map<String, PieChart> m_distributions;

	public Map<String, PieChart> getDistributions() {
		return m_distributions;
	}

	public List<LogMsg> getErrors() {
		return m_errors;
	}

	public List<String> getLevels() {
		return m_levels;
	}

	public List<String> getModules() {
		return m_modules;
	}

	public int getTotalCount() {
		return m_totalCount;
	}

	public void setDistributions(Map<String, PieChart> distributions) {
		m_distributions = distributions;
	}

	public void setErrors(List<LogMsg> errors) {
		m_errors = errors;
	}

	public void setLevels(List<String> levels) {
		m_levels = levels;
	}

	public void setModules(List<String> modules) {
		m_modules = modules;
	}

	public void setTotalCount(int totalCount) {
		m_totalCount = totalCount;
	}

}
