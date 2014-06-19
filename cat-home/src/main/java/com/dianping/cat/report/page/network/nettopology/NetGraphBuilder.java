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

	private static final int ERROR = 3;

	public NetGraphSet buildGraphSet(NetGraph netGraphTemplate, Map<String, MetricReport> reports, List<String> alertKeys) {
		NetGraphSet netGraphSet = new NetGraphSet();

		for (int minute = 0; minute <= 59; minute++) {
			NetGraph netGraph = copyBaseInfoFromTemplate(netGraphTemplate);

			for (NetTopology netTopology : netGraph.getNetTopologies()) {
				List<String> alertSwitchs = new ArrayList<String>();

				for (Connection connection : netTopology.getConnections()) {
					try {
						buildConnectionInfo(reports, alertKeys, minute, alertSwitchs, connection);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}

				for (Switch sw : netTopology.getSwitchs()) {
					if (alertSwitchs.contains(sw.getName())) {
						sw.setState(ERROR);
					}
				}
			}
			netGraph.setMinute(minute);
			netGraphSet.addNetGraph(netGraph);
		}

		return netGraphSet;
	}

	private void buildConnectionInfo(Map<String, MetricReport> reports, List<String> alertKeys, int minute,
	      List<String> alertSwitchs, Connection connection) {
		double insum = 0, outsum = 0, inDiscardsSum = 0, outDiscardsSum = 0, inErrorsSum = 0, outErrorsSum = 0;
		int inState = 0, outState = 0, inDiscardsState = 0, outDiscardsState = 0, inErrorsState = 0, outErrorsState = 0;

		for (Interface inter : connection.getInterfaces()) {
			String group = inter.getGroup();
			MetricReport report = reports.get(group);
			String domain = inter.getDomain();
			String key = inter.getKey();

			updateInterface(inter, report, minute);

			if (inAlert(alertKeys, domain, key)) {
				inter.setInstate(ERROR);
				inState = ERROR;
			}
			if (inDiscardsAlert(alertKeys, domain, key)) {
				inter.setInDiscardsState(ERROR);
				inDiscardsState = ERROR;
			}
			if (inErrorsAlert(alertKeys, domain, key)) {
				inter.setInErrorsState(ERROR);
				inErrorsState = ERROR;
			}
			if (outAlert(alertKeys, domain, key)) {
				inter.setOutstate(ERROR);
				outState = ERROR;
			}
			if (outDiscardsAlert(alertKeys, domain, key)) {
				inter.setOutDiscardsState(ERROR);
				outDiscardsState = ERROR;
			}
			if (outErrorsAlert(alertKeys, domain, key)) {
				inter.setOutErrorsState(ERROR);
				outErrorsState = ERROR;
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
		
		if (inState == ERROR || outState == ERROR || inDiscardsState == ERROR || outDiscardsState == ERROR
		      || inErrorsState == ERROR || outErrorsState == ERROR) {
			alertSwitchs.add(connection.getFrom());
		}
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
			Cat.logError(e);
		}
	}

}
