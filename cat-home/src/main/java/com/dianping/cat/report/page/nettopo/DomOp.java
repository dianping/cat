package com.dianping.cat.report.page.nettopo;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomOp {
	private Node node;
	
	public DomOp(Node node) {
		this.node = node;
	}
	
	public String getAttribute(String key) {
		String val;
		try {
			val = node.getAttributes().getNamedItem(key).getNodeValue();
		}
		catch (Exception e) {
			val = "";
		}
		return val;
	}
	
	public int getAttrInt(String key) {
		int val;
		try {
			val = Integer.parseInt(node.getAttributes().getNamedItem(key).getNodeValue());
		}
		catch (Exception e) {
			val = 0;
		}
		return val;
	}
	
	public ArrayList<Node> getChildNodes(String tag) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (child.getNodeName().equalsIgnoreCase(tag))
				nodes.add(child);
		}
		
		return nodes;
	}
}
