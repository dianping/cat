package com.dianping.cat.report.page.network.influx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.network.entity.Anchor;
import com.dianping.cat.home.network.entity.Connection;
import com.dianping.cat.home.network.entity.Interface;
import com.dianping.cat.home.network.entity.NetGraph;
import com.dianping.cat.home.network.entity.NetTopology;
import com.dianping.cat.home.network.entity.Switch;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.MetricType;
import com.dianping.cat.server.QueryParameter;

public class InfluxNetGraphBuilder {

	@Inject
	private MetricService m_metricService;

	private static final String CATEGORY = "network";

	private static final String FLOW_IN = "flow-in";

	private static final String FLOW_OUT = "flow-out";

	private static final String IN_ERROR = "discard/error-inerrors";

	private static final String OUT_ERROR = "discard/error-outerrors";

	private static final String IN_DISCARDS = "discard/error-indiscards";

	private static final String OUT_DISCARDS = "discard/error-outdiscards";

	private static final int ERROR = 3;

	private void buildConnectionInfo(List<AlertEntity> alerts, Date minute, List<String> alertSwitchs,
	      Connection connection) {
		double insum = 0, outsum = 0, inDiscardsSum = 0, outDiscardsSum = 0, inErrorsSum = 0, outErrorsSum = 0;
		int inState = 0, outState = 0, inDiscardsState = 0, outDiscardsState = 0, inErrorsState = 0, outErrorsState = 0;

		for (Interface inter : connection.getInterfaces()) {
			updateInterface(inter, minute);

			if (containsAlert(alerts, inter, FLOW_IN)) {
				inter.setInstate(ERROR);
				inState = ERROR;
			}
			if (containsAlert(alerts, inter, IN_DISCARDS)) {
				inter.setInDiscardsState(ERROR);
				inDiscardsState = ERROR;
			}
			if (containsAlert(alerts, inter, IN_ERROR)) {
				inter.setInErrorsState(ERROR);
				inErrorsState = ERROR;
			}
			if (containsAlert(alerts, inter, FLOW_OUT)) {
				inter.setOutstate(ERROR);
				outState = ERROR;
			}
			if (containsAlert(alerts, inter, OUT_DISCARDS)) {
				inter.setOutDiscardsState(ERROR);
				outDiscardsState = ERROR;
			}
			if (containsAlert(alerts, inter, OUT_ERROR)) {
				inter.setOutErrorsState(ERROR);
				outErrorsState = ERROR;
			}
			if (containsAlert(alerts, inter, "status-up/down")) {
				inter.setInstate(ERROR).setOutstate(ERROR);
				inter.setInDiscardsState(ERROR).setOutDiscardsState(ERROR);
				inter.setInErrorsState(ERROR).setOutErrorsState(ERROR);
				inState = outState = ERROR;
				inDiscardsState = outDiscardsState = ERROR;
				inErrorsState = outErrorsState = ERROR;
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

	public NetGraph buildGraphSet(NetGraph netGraphTemplate, Date minute, List<AlertEntity> alerts) {
		NetGraph netGraph = copyBaseInfoFromTemplate(netGraphTemplate);

		for (NetTopology netTopology : netGraph.getNetTopologies()) {
			List<String> alertSwitchs = new ArrayList<String>();

			for (Connection connection : netTopology.getConnections()) {
				try {
					buildConnectionInfo(alerts, minute, alertSwitchs, connection);
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
		return netGraph;
	}

	private boolean containsAlert(List<AlertEntity> alertKeys, Interface inter, String suffix) {
		for (AlertEntity alert : alertKeys) {
			if (alert.getGroup().contains(inter.getGroup()) && alert.getGroup().contains(inter.getKey())
			      && alert.getMetric().equals(CATEGORY + "." + suffix)) {
				return true;
			}
		}
		return false;
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

	private double querValue(Interface inter, Date minute, String measure) {
		QueryParameter p = new QueryParameter();

		p.setCategory(CATEGORY).setInterval("1m").setStart(minute)
		      .setEnd(new Date(minute.getTime() + TimeHelper.ONE_MINUTE));
		p.setMeasurement(CATEGORY + "." + measure).setType(MetricType.SUM);

		StringBuilder sb = new StringBuilder();

		sb.append("endPoint='").append(inter.getGroup()).append("';port='").append(inter.getKey()).append("'");
		p.setTags(sb.toString());

		Map<Long, Double> value = m_metricService.query(p);

		if (!value.isEmpty()) {
			return value.values().iterator().next();
		} else {
			return 0;
		}
	}

	private void updateInterface(Interface inter, Date minute) {
		double flowin = querValue(inter, minute, FLOW_IN);
		double flowout = querValue(inter, minute, FLOW_OUT);
		double indis = querValue(inter, minute, IN_DISCARDS);
		double outdis = querValue(inter, minute, OUT_DISCARDS);
		double inerr = querValue(inter, minute, IN_ERROR);
		double outerr = querValue(inter, minute, OUT_ERROR);

		inter.setIn(flowin / 60 * 8);
		inter.setOut(flowout / 60 * 8);
		inter.setInDiscards(indis / 60);
		inter.setOutDiscards(outdis / 60);
		inter.setInErrors(inerr / 60);
		inter.setOutErrors(outerr / 60);
	}
}
