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
						int inDiscardsState = 0;
						int outDiscardsState = 0;
						int inErrorsState = 0;
						int outErrorsState = 0;

						for (Interface inter : connection.getInterfaces()) {
							String group = inter.getGroup();
							MetricReport report = reports.get(group);

							updateInterface(inter, report, i);

							String domain = inter.getDomain();
							String key = inter.getKey();

							if (inAlert(alertKeys, domain, key)) {
								inter.setInstate(3);
								inState = 3;
							}
							if (inDiscardsAlert(alertKeys, domain, key)) {
								inter.setInDiscardsState(3);
								inDiscardsState = 3;
							}if (inErrorsAlert(alertKeys, domain, key)) {
								inter.setInErrorsState(3);
								inErrorsState = 3;
							}
							if (outAlert(alertKeys, domain, key)) {
								inter.setOutstate(3);
								outState = 3;
							}
							if (outDiscardsAlert(alertKeys, domain, key)) {
								inter.setOutDiscardsState(3);
								outDiscardsState = 3;
							}if (outErrorsAlert(alertKeys, domain, key)) {
								inter.setOutErrorsState(3);
								outErrorsState = 3;
							}

							insum += inter.getIn();
							outsum += inter.getOut();
							inDiscardsSum += inter.getInDiscards();
							outDiscardsSum += inter.getOutDiscards();
							inErrorsSum += inter.getInErrors();
							outErrorsSum += inter.getOutErrors();
						}
						connection.setInsum(insum);
						connection.setOutsum(outsum);
						connection.setInDiscards(inDiscardsSum);
						connection.setOutDiscards(outDiscardsSum);
						connection.setInErrors(inErrorsSum);
						connection.setOutErrors(outErrorsSum);
						connection.setInstate(inState);
						connection.setOutstate(outState);
						connection.setInDiscardsState(inDiscardsState);
						connection.setOutDiscardsState(outDiscardsState);
						connection.setInErrorsState(inErrorsState);
						connection.setOutErrorsState(outErrorsState);
						if (inState == 3 || outState == 3 || inDiscardsState == 3 || outDiscardsState == 3 ||
								inErrorsState == 3 || outErrorsState == 3) {
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

	private boolean inAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-flow-in");
	}
	
	private boolean inDiscardsAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-discard/error-indiscards");
	}
	
	private boolean inErrorsAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-discard/error-inerrors");
	}
	
	private boolean outAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-flow-out");
	}
	
	private boolean outDiscardsAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-discard/error-outdiscards");
	}
	
	private boolean outErrorsAlert(List<String> alertKeys, String domain, String key) {
		return alertKeys.contains(domain + ":Metric:" + key + "-discard/error-outerrors");
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
					interB.setInDiscardsState(interA.getInDiscardsState());
					interB.setOutDiscardsState(interA.getOutDiscardsState());
					interB.setInErrorsState(interA.getInErrorsState());
					interB.setOutErrorsState(interA.getOutErrorsState());
					connectionB.addInterface(interB);
				}
				connectionB.setInsum(connectionA.getInsum());
				connectionB.setOutsum(connectionA.getOutsum());
				connectionB.setFrom(connectionA.getFrom());
				connectionB.setTo(connectionA.getTo());
				connectionB.setInstate(connectionA.getInstate());
				connectionB.setOutstate(connectionA.getOutstate());
				connectionB.setInDiscardsState(connectionA.getInDiscardsState());
				connectionB.setOutDiscardsState(connectionA.getOutDiscardsState());
				connectionB.setInErrorsState(connectionA.getInErrorsState());
				connectionB.setOutErrorsState(connectionA.getOutErrorsState());
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
			MetricItem inDiscardsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key
			      + "-discard/error-indiscards");
			MetricItem outDiscardsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key
			      + "-discard/error-outdiscards");
			MetricItem inErrorsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-discard/error-inerrors");
			MetricItem outErrorsItem = report.findOrCreateMetricItem(domain + ":Metric:" + key
			      + "-discard/error-outerrors");

			inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setInDiscards(inDiscardsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setOutDiscards(outDiscardsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setInErrors(inErrorsItem.findOrCreateSegment(minute).getSum() / 60);
			inter.setOutErrors(outErrorsItem.findOrCreateSegment(minute).getSum() / 60);
		} catch (Exception e) {
			inter.setIn(0.0);
			inter.setOut(0.0);
			inter.setInDiscards(0.0);
			inter.setOutDiscards(0.0);
			inter.setInErrors(0.0);
			inter.setOutErrors(0.0);
			Cat.logError(e);
		}
	}

}
