package com.dianping.cat.report;

import com.site.web.mvc.Page;
import com.site.web.mvc.annotation.ModuleMeta;

public enum ReportPage implements Page {

	DASHBOARD("dashboard", "dashboard", "Dashboard", "Dashboard", false),

	EVENT("event", "e", "Event", "Event Summary Report", true),

	HEARTBEAT("heartbeat", "h", "Heartbeat", "Heartbeat Summary Report", true),

	HEATMAP("heatmap", "heatmap", "Heatmap", "Heatmap", false),

	HOME("home", "home", "Home", "Home Page", true),

	IP("ip", "ip", "Top IP", "Top Visited IP", false),

	LOGVIEW("logview", "m", "Logview", "Log View Details", false),

	MATRIX("matrix", "matrix", "Matrix", "Matrix", true),

	MODEL("model", "model", "Model", "Service Model", false),

	MONTHREPORT("monthreport", "monthreport", "Monthreport", "Monthreport", false),

	PROBLEM("problem", "p", "Problem", "Problem Discovered", true),

	SQL("sql", "sql", "SQL", "SQL Report", false),

	TASK("task", "task", "Task", "Task", false),

	TRANSACTION("transaction", "t", "Transaction", "Transaction Summary Report", true);

	public static ReportPage getByName(String name, ReportPage defaultPage) {
		for (ReportPage action : ReportPage.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultPage;
	}

	private String m_description;

	private String m_name;

	private String m_path;

	private boolean m_standalone;

	private String m_title;

	private ReportPage(String name, String path, String title, String description, boolean standalone) {
		m_name = name;
		m_path = path;
		m_title = title;
		m_description = description;
		m_standalone = standalone;
	}

	public String getDescription() {
		return m_description;
	}

	public String getModuleName() {
		ModuleMeta meta = ReportModule.class.getAnnotation(ModuleMeta.class);

		if (meta != null) {
			return meta.name();
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getPath() {
		return m_path;
	}

	public String getTitle() {
		return m_title;
	}

	public ReportPage[] getValues() {
		return ReportPage.values();
	}

	public boolean isStandalone() {
		return m_standalone;
	}
}
