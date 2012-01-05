package com.dianping.cat.message.consumer.model.failure;

public class FailureReportAnalyzerConfig {
	private String m_machines;
	private String m_handlers;

	public String getMachines() {
		return m_machines;
	}

	public void setMachines(String machines) {
		m_machines = machines;
	}

	public String getHandlers() {
		return m_handlers;
	}

	public void setHandlers(String handlers) {
		m_handlers = handlers;
	}
}
