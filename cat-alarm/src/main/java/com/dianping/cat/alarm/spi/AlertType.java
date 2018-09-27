package com.dianping.cat.alarm.spi;

public enum AlertType {

	Business("Business", "业务告警"),

	Network("Network", "网络告警"),

	DataBase("Database", "数据库告警"),

	System("System", "系统告警"),

	Exception("Exception", "异常告警"),

	HeartBeat("Heartbeat", "心跳告警"),

	ThirdParty("ThirdParty", "第三方告警"),

	FrontEndException("FrontEnd", "前端告警"),

	JS("Js", "JS错误告警"),

	App("App", "APP接口告警"),

	Ajax("Ajax", "Ajax访问告警"),

	Transaction("Transaction", "Transacation告警"),

	Event("Event", "Event告警"),

	STORAGE_SQL("SQL", "数据库大盘告警"),

	STORAGE_CACHE("Cache", "缓存大盘告警"),

	STORAGE_RPC("RPC", "服务大盘告警"),

	SERVER_NETWORK("ServerNetwork", "网络告警"),

	SERVER_SYSTEM("ServerSystem", "系统告警"),

	SERVER_DATABASE("ServerDatabase", "数据库告警"),
	
	CRASH("Crash", "Crash告警");

	private String m_name;

	private String m_title;

	public static AlertType getTypeByName(String name) {
		for (AlertType type : AlertType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	private AlertType(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

}
