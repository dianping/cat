package com.dianping.cat.system.page.abtest;

public enum ABTestEntityStatus {

	RUNNING(0), STOPPED(1), READY(2), DISABLED(3),DEFALUT(4);

	public static ABTestEntityStatus getByName(String name, ABTestEntityStatus defaultStatus) {
		for (ABTestEntityStatus status : ABTestEntityStatus.values()) {
			if (status.getName().equalsIgnoreCase(name)) {
				return status;
			}
		}

		return defaultStatus;
	}

	private int m_status;

	private static final String[] s_status = { "Running", "Stopped", "Ready", "Disabled", "Default" };

	private ABTestEntityStatus(int status) {
		m_status = status;
	}

	public int getStatus() {
		return m_status;
	}

	private String getName() {
		return s_status[m_status];
	}
}
