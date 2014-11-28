package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

public class StatusExtensionRegister {

	private List<StatusExtension> m_extensions = new ArrayList<StatusExtension>();

	public static StatusExtensionRegister s_register = new StatusExtensionRegister();

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	private StatusExtensionRegister() {
	}

	public List<StatusExtension> getStatusExtension() {
		return m_extensions;
	}

	public void register(StatusExtension monitor) {
		m_extensions.add(monitor);
	}
}
