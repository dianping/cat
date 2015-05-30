package com.dianping.cat.report.alert;

public enum AlertType {

	Business("Business"),

	Network("Network"),

	DataBase("Database"),

	System("System"),

	Exception("Exception"),

	HeartBeat("Heartbeat"),

	ThirdParty("ThirdParty"),

	FrontEndException("FrontEnd"),

	App("App"),

	Web("Web"),

	Transaction("Transaction"),

	STORAGE_SQL("SQL"),

	STORAGE_CACHE("Cache");

	private String m_name;

	public static AlertType getTypeByName(String name) {
		for (AlertType type : AlertType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	private AlertType(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
