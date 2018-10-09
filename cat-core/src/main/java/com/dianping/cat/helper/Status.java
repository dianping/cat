package com.dianping.cat.helper;

public enum Status {

	NO_MAP(0), // 原始日志没有被混淆

	NOT_MAPPED(1), // 原始日志被混淆，还没有反混淆

	MAPPING(2), // 正在反混淆

	MAPPED(3), // 原始日志被混淆，且已经反混淆

	FAILED(4); // 反混淆失败

	private int m_status;

	private Status(int status) {
		m_status = status;
	}

	public int getStatus() {
		return m_status;
	}

}
