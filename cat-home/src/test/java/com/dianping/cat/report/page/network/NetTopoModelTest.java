package com.dianping.cat.report.page.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.home.network.entity.NetGraph;
import com.dianping.cat.home.network.entity.NetGraphSet;
import com.dianping.cat.home.network.transform.DefaultXmlBuilder;
import com.dianping.cat.home.network.transform.DefaultSaxParser;

@RunWith(JUnit4.class)
public class NetTopoModelTest {

	@Test
	public void test() throws Exception {
		NetGraphSet netGraphSet = new NetGraphSet();
		NetGraph netGraph = new NetGraph();
		netGraphSet.getNetGraphs().put(0, netGraph);
		netGraph = new NetGraph();
		netGraph.setMinute(1);
		netGraphSet.getNetGraphs().put(1, netGraph);
		System.out.println("0:"+netGraphSet.getNetGraphs().get(0));
		System.out.println("1:"+netGraphSet.getNetGraphs().get(1));
		DefaultXmlBuilder defaultXmlBuilder = new DefaultXmlBuilder();
		System.out.println(netGraphSet);
		String content = defaultXmlBuilder.buildXml(netGraphSet);
		System.out.println(content);
		NetGraphSet nGS = DefaultSaxParser.parse(content);
		System.out.println(nGS);
	}

}
