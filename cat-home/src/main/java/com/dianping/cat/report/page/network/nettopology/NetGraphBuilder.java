package com.dianping.cat.report.page.network.nettopology;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

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
import com.dianping.cat.home.nettopo.transform.DefaultSaxParser;

public class NetGraphBuilder implements Initializable {

	private NetGraph m_netGraphTemplate;

	private Set<String> m_requireGroups;

	public Set<String> buildRequireGroups() {
		return m_requireGroups;
	}

	public NetGraphSet buildGraphSet(Map<String, MetricReport> reports) {
		NetGraphSet netGraphSet = new NetGraphSet();

		for (int i = 0; i <= 59; i++) {
			NetGraph netGraph = copyBaseInfoFromTemplate(m_netGraphTemplate);
			for (NetTopology netTopology : netGraph.getNetTopologies()) {
				for (Connection connection : netTopology.getConnections()) {
					double insum = 0;
					double outsum = 0;

					for (Interface inter : connection.getInterfaces()) {
						String group = inter.getGroup();
						MetricReport report = reports.get(group);

						updateInterface(inter, report, i);
						insum += inter.getIn();
						outsum += inter.getOut();
					}
					connection.setInsum(insum);
					connection.setOutsum(outsum);
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
					interB.setState(interA.getState());
					connectionB.addInterface(interB);
				}
				connectionB.setInsum(connectionA.getInsum());
				connectionB.setOutsum(connectionA.getOutsum());
				connectionB.setFrom(connectionA.getFrom());
				connectionB.setTo(connectionA.getTo());
				connectionB.setState(connectionA.getState());
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
			MetricItem inItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-in");
			MetricItem outItem = report.findOrCreateMetricItem(domain + ":Metric:" + key + "-out");

			inter.setIn(inItem.findOrCreateSegment(minute).getSum() / 60 * 8);
			inter.setOut(outItem.findOrCreateSegment(minute).getSum() / 60 * 8);
		} catch (Exception e) {
			inter.setIn(0.0);
			inter.setOut(0.0);
			Cat.logError(e);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		InputStream is = NetGraphManager.class.getResourceAsStream("/config/default-nettopology-config.xml");
		NetGraphSet netGraphSet = null;

		try {
			netGraphSet = DefaultSaxParser.parse(is);
			m_netGraphTemplate = netGraphSet.getNetGraphs().get(0);
			is.close();

			m_requireGroups = new HashSet<String>();

			for (NetTopology netTopology : m_netGraphTemplate.getNetTopologies()) {
				for (Connection connection : netTopology.getConnections()) {
					for (Interface inter : connection.getInterfaces()) {
						m_requireGroups.add(inter.getGroup());
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
}
