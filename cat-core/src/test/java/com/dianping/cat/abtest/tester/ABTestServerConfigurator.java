package com.dianping.cat.abtest.tester;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class ABTestServerConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ABTestGroupStrategy.class, RetinaImgGroupStrategy.ID, RetinaImgGroupStrategy.class)
				.is(PER_LOOKUP));
		
		return all;
	}

	@Override
	protected Class<?> getTestClass() {
		return RetinaImgGroupStrategy.class;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ABTestServerConfigurator());
	}
}
