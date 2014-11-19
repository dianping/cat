package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void register(String name, List<Property> properties) {
		Map<String, Property> map = new HashMap<String, Property>();

		for (Property p : properties) {
			map.put(p.getId(), p);
		}
		m_extensions.add(new Property(name).setValue(map));
	}

	public void register(Property property) {
		m_extensions.add(property);
	}
}
