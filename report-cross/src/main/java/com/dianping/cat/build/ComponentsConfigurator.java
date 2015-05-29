package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineTransactionComponents());
		all.addAll(defineServiceComponents());
		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineServiceComponents() {
		List<Component> all = new ArrayList<Component>();

		return all;
	}

	private Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
