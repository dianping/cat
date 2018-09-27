package com.dianping.cat.influxdb.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.build.CatDatabaseConfigurator;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.influxdb.service.DataSourceServiceImpl;
import com.dianping.cat.influxdb.service.MetricServiceImpl;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.add(A(InfluxDBConfigManager.class));
		all.add(A(MetricServiceImpl.class));
		all.add(A(DataSourceServiceImpl.class));

		return all;
	}
}
