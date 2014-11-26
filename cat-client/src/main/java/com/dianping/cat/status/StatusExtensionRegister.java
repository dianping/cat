package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.status.model.entity.Extension;

public class StatusExtensionRegister {

	private List<Extension> m_extensions = new ArrayList<Extension>();

	public static StatusExtensionRegister s_register = new StatusExtensionRegister();

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	private StatusExtensionRegister() {
	}

	public List<Extension> geteExtensions() {
		return m_extensions;
	}

	public void register(Extension extension) {
		m_extensions.add(extension);
	}
}
