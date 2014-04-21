package com.dianping.cat.report.page.nettopo.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.nettopo.DomOp;

public class NetTopology {
	private String name;

	private HashMap<String, Anchor> anchors;

	private HashMap<String, Switch> switchs;

	private ArrayList<Connection> connections;

	public NetTopology(Node netTopologyNode) {
		DomOp domOp = new DomOp(netTopologyNode);
		name = domOp.getAttribute("name");
		anchors = new HashMap<String, Anchor>();
		switchs = new HashMap<String, Switch>();
		connections = new ArrayList<Connection>();

		for (Node node : domOp.getChildNodes("anchors")) {
			for (Node n : (new DomOp(node)).getChildNodes("anchor")) {
				Anchor an = new Anchor(n);
				anchors.put(an.getName(), an);
			}
		}

		for (Node node : domOp.getChildNodes("switchs")) {
			for (Node n : (new DomOp(node)).getChildNodes("switch")) {
				Switch sw = new Switch(n);
				switchs.put(sw.getName(), sw);
			}
		}

		for (Node node : domOp.getChildNodes("connections")) {
			for (Node n : (new DomOp(node)).getChildNodes("connection")) {
				connections.add(new Connection(n));
			}
		}
	}

	public String getJsonData() {
		StringBuilder sbJson = new StringBuilder();

		sbJson.append("{");
		sbJson.append("'anchor':{");
		for (String key : anchors.keySet()) {
			anchors.get(key).appendToJson(sbJson);
		}
		sbJson.append("},");

		sbJson.append("'sw':{");
		for (String key : switchs.keySet()) {
			switchs.get(key).appendToJson(sbJson);
		}
		sbJson.append("},");

		sbJson.append("'conn':[");
		for (Connection connection : connections) {
			connection.appendToJson(sbJson);
		}
		sbJson.append("],");
		sbJson.append("}");

		return sbJson.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(HashMap<String, Anchor> anchors) {
		this.anchors = anchors;
	}

	public HashMap<String, Switch> getSwitchs() {
		return switchs;
	}

	public void setSwitchs(HashMap<String, Switch> switchs) {
		this.switchs = switchs;
	}

	public ArrayList<Connection> getConnections() {
		return connections;
	}

	public void setConnections(ArrayList<Connection> connections) {
		this.connections = connections;
	}
}
