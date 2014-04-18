package com.dianping.cat.report.page.nettopo;

import java.util.ArrayList;
import org.w3c.dom.Node;    


public class NetTopology {
	private String name;
	private ArrayList<Anchor> anchors;
	private ArrayList<Switch> switchs;
	private ArrayList<Connection> connections;
	
	
	public NetTopology(Node netTopologyNode) {
		DomOp domOp = new DomOp(netTopologyNode);
		name = domOp.getAttribute("name");
		anchors = new ArrayList<Anchor>();
		switchs = new ArrayList<Switch>();
		connections = new ArrayList<Connection>();
		
		for (Node node : domOp.getChildNodes("anchors")) {
			for (Node n : (new DomOp(node)).getChildNodes("anchor")) {
				anchors.add(new Anchor(n));
			}
		}
		
		for (Node node : domOp.getChildNodes("switchs")) {
			for (Node n : (new DomOp(node)).getChildNodes("switch")) {
				switchs.add(new Switch(n));
			}
		}
		
		for (Node node : domOp.getChildNodes("connections")) {
			for (Node n : (new DomOp(node)).getChildNodes("connection")) {
				connections.add(new Connection(n));
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(ArrayList<Anchor> anchors) {
		this.anchors = anchors;
	}

	public ArrayList<Switch> getSwitchs() {
		return switchs;
	}

	public void setSwitchs(ArrayList<Switch> switchs) {
		this.switchs = switchs;
	}

	public ArrayList<Connection> getConnections() {
		return connections;
	}

	public void setConnections(ArrayList<Connection> connections) {
		this.connections = connections;
	}
}
