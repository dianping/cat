package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.status.model.entity.Property;

public class StatusExtensionRegister {

	private List<Property> m_extensions = new ArrayList<Property>();

	public static StatusExtensionRegister s_register = new StatusExtensionRegister();

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	private StatusExtensionRegister() {
	}

	public List<Property> getExtentionProperties() {
		return m_extensions;
	}

	public void register(Property property) {
		m_extensions.add(property);
	}
}
