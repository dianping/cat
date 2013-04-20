package com.dianping.cat.hadoop.sql;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.io.TransportManager;

public class SqlJobDataProduceTestConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(TransportManager.class, MockTransportManager.class));

		return all;
	}

	@Override
	protected Class<?> getTestClass() {
		return SqlJobDataProduceTest.class;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new SqlJobDataProduceTestConfigurator());
	}
}
