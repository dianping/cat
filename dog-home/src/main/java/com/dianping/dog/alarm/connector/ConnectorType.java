package com.dianping.dog.alarm.connector;

public enum ConnectorType {

	HTTP(0), TCP(1), UNSUPPORT(2);

	@SuppressWarnings("unused")
   private int type;

	private ConnectorType(int type) {
		this.type = type;
	}

}
