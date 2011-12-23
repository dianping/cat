package com.dianping.cat.transport;

public class TransportManager {
	private static TransportManager s_instance;

	private Transport m_transport;

	public static Transport getTransport() {
		if (s_instance == null) {
			throw new RuntimeException("Please call method setTransport() to initialize first!");
		}

		return s_instance.m_transport;
	}

	public void setTransport(Transport transport) {
		if (transport == null) {
			s_instance = null;
		} else {
			s_instance = new TransportManager();
			s_instance.m_transport = transport;
		}
	}
}
