package com.dianping.cat.report.page.task;

public enum JspFile {
	VIEW("/jsp/report/task.jsp"),

	REDO("/jsp/report/taskRedo.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
