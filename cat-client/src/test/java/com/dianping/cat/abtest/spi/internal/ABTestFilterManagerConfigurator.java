//package com.dianping.cat.abtest.spi.internal;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.unidal.lookup.configuration.AbstractResourceConfigurator;
//import org.unidal.lookup.configuration.Component;
//
//import com.dianping.cat.abtest.spi.internal.ABTestFilterManagerTest.TestCondition;
//import com.dianping.cat.abtest.spi.internal.conditions.ABTestCondition;
//
//public class ABTestFilterManagerConfigurator extends AbstractResourceConfigurator {
//	@Override
//	public List<Component> defineComponents() {
//		List<Component> all = new ArrayList<Component>();
//		
//		all.add(C(ABTestCondition.class, TestCondition.ID0, TestCondition.class).config(E("march").value("true")));
//		all.add(C(ABTestCondition.class, TestCondition.ID1, TestCondition.class).config(E("march").value("false")));
//
//		return all;
//	}
//
//	@Override
//	protected Class<?> getTestClass() {
//		return ABTestFilterManagerTest.class;
//	}
//
//	public static void main(String[] args) {
//		generatePlexusComponentsXmlFile(new ABTestFilterManagerConfigurator());
//	}
//}
