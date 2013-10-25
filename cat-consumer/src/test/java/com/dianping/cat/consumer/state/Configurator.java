package com.dianping.cat.consumer.state;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return StateAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockReportManager.class)//
		      .req(ReportDelegate.class, ID, "m_delegate"));
		all.add(C(ReportDelegate.class, ID, ExtendedStateDelegate.class));
		all.add(C(MessageAnalyzer.class, ID, StateAnalyzer.class).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerStatisticManager.class).config(E("m_ip").value("192.168.1.1")));
		
		return all;
	}
	
	public static class ExtendedStateDelegate extends StateDelegate{
	}
}
