package com.dianping.cat.report.page.task;

public enum JspFile {
	REDO("/jsp/report/taskRedo.jsp"),

	VIEW("/jsp/report/task.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
