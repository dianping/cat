package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

public class StatusExtensionRegister {

	public static StatusExtensionRegister s_register = new StatusExtensionRegister();

	private List<StatusExtension> m_extensions = new ArrayList<StatusExtension>();

	private StatusExtensionRegister() {
	}

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	public List<StatusExtension> getStatusExtension() {
		synchronized (this) {
			return m_extensions;
		}
	}

	public void register(StatusExtension monitor) {
		synchronized (this) {
			m_extensions.add(monitor);
		}
	}

	public void unregister(StatusExtension monitor) {
		synchronized (this) {
			m_extensions.remove(monitor);
		}
	}
}
