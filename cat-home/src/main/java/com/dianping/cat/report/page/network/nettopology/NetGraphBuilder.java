package com.dianping.cat.report.page.network.nettopology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.home.nettopo.entity.Anchor;
import com.dianping.cat.home.nettopo.entity.Connection;
import com.dianping.cat.home.nettopo.entity.Interface;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.home.nettopo.entity.Switch;

public class NetGraphBuilder {

	public NetGraphSet buildGraphSet(NetGraph netGraphTemplate, Map<String, MetricReport> reports, List<String> alertKeys) {
		NetGraphSet netGraphSet = new NetGraphSet();

		for (int i = 0; i <= 59; i++) {
			NetGraph netGraph = copyBaseInfoFromTemplate(netGraphTemplate);
			for (NetTopology netTopology : netGraph.getNetTopologies()) {
				List<String> alertSwitchs = new ArrayList<String>();
				
				for (Connection connection : netTopology.getConnections()) {
					try {
						double insum = 0;
						double outsum = 0;
						double inDiscardsSum = 0;
						double outDiscardsSum = 0;
						double inErrorsSum = 0;
						double outErrorsSum = 0;
						int inState = 0;
						int outState = 0;

						for (Interface inter : connection.getInterfaces()) {
							String group = inter.getGroup();
							MetricReport report = reports.get(group);

							updateInterface(inter, report, i);
							
							if (alertKeys.contains(inter.getKey() + "-flow-in") || 
									alertKeys.contains(inter.getKey() + "-discard/error-indiscards") ||
									alertKeys.contains(inter.getKey() + "-discard/error-inerrors")) {
								inter.setInstate(3);
								inState = 3;
							}
							if (alertKeys.contains(inter.getKey() + "-flow-out") ||
									alertKeys.contains(inter.getKey() + "-discard/error-outdiscards") ||
									alertKeys.contains(inter.getKey() + "-discard/error-outerrors")) {
								inter.setOutstate(3);
								outState = 3;
							}
							
							insum += inter.getIn();
							outsum += inter.getOut();
							inDiscardsSum += inter.getIndiscards();
							outDiscardsSum += inter.getOutdiscards();
							inErrorsSum += inter.getInerrors();
							outErrorsSum += inter.getOuterrors();
						}
						connection.setInsum(insum);
						connection.setOutsum(outsum);
						connection.setIndiscards(inDiscardsSum);
						connection.setOutdiscards(outDiscardsSum);
						connection.setInerrors(inErrorsSum);
						connection.setOuterrors(outErrorsSum);
						connection.setInstate(inState);
						connection.setOutstate(outState);
						if (inState == 3 || outState == 3) {
							alertSwitchs.add(connection.getFrom());
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				
				for (Switch sw : netTopology.getSwitchs()) {
					if (alertSwitchs.contains(sw.getName())) {
						sw.setState(3);
					}
				}
			}
			netGraph.setMinute(i);
			netGraphSet.addNetGraph(netGraph);
		}

		return netGraphSet;
	}

	private NetGraph copyBaseInfoFromTemplate(NetGraph netGraph) {
		NetGraph to = new NetGraph();
		for (NetTopology netTopologyA : netGraph.getNetTopologies()) {
			NetTopology netTopologyB = new NetTopology();

			for (Anchor anchorA : netTopologyA.getAnchors()) {
				Anchor anchorB = new Anchor();
				anchorB.setName(anchorA.getName());
				anchorB.setX(anchorA.getX());
				anchorB.setY(anchorA.getY());
				netTopologyB.addAnchor(anchorB);
			}

			for (Switch switchA : netTopologyA.getSwitchs()) {
				Switch switchB = new Switch();
				switchB.setName(switchA.getName());
				switchB.setX(switchA.getX());
				switchB.setY(switchA.getY());
				switchB.setState(switchA.getState());
				netTopologyB.addSwitch(switchB);
			}

			for (Connection connectionA : netTopologyA.getConnections()) {
				Connection connectionB = new Connection();
				for (Interface interA : connectionA.getInterfaces()) {
					Interface interB = new Interface();
					interB.setDomain(interA.getDomain());
					interB.setGroup(interA.getGroup());
					interB.setKey(interA.getKey());
					interB.setIn(interA.getIn());
					interB.setOut(interA.getOut());
					interB.setInstate(interA.getInstate());
					interB.setOutstate(interA.getOutstate());
					interB.setInstate(interA.getInstate());
					interB.setOutstate(interA.getOutstate());
					connectionB.addInterface(interB);
				}
				connectionB.setInsum(connectionA.getInsum());
				connectionB.setOutsum(connectionA.getOutsum());
				connectionB.setFrom(connectionA.getFrom());
				connectionB.setTo(connectionA.getTo());
				connectionB.setInstate(connectionA.getInstate());
				connectionB.setOutstate(connectionA.getOutstate());
				netTopologyB.addConnection(connectionB);
			}

			to.addNetTopology(netTopologyB);
		}

		return to;
	}

	private void updateInterface(Interface inter, MetricReport report, int minute) {
		String domain = inter.getDomain();
		String key = inter.getKey();

		try {
			MetricItem inItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-flow-in");
			MetricItem outItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-flow-out");
			MetricItem inDiscardsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-discard/error-indiscards");
			MetricItem outDiscardsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-discard/error-outdiscards");
			MetricItem inErrorsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-discard/error-inerrors");
			MetricItem outErrorsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-discard/error-outerrors");

			inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setIndiscards(inDiscardsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setOutdiscards(outDiscardsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setInerrors(inErrorsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setOuterrors(outErrorsItem.findOrCreateSegment(minute).getSum() / 60);
		} catch (Exception e) {
			inter.setIn(0.0);
			inter.setOut(0.0);
			inter.setIndiscards(0.0);
			inter.setOutdiscards(0.0);
			inter.setInerrors(0.0);
			inter.setOuterrors(0.0);
			Cat.logError(e);
		}
	}

}
