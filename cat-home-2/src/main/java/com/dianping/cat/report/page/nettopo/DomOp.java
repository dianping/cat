package com.dianping.cat.report.page.nettopo;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dianping.cat.Cat;

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
	
	public double getAttrDouble(String key) {
		
		double val;
		String strVal = "";
		try {
			strVal = node.getAttributes().getNamedItem(key).getNodeValue();
			val = parseDouble(strVal);
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
	
	private double parseDouble(String strVal) {
		int length = strVal.length();
		if (length == 0)
			return 0;
		
		char unit = strVal.charAt(length - 1);
		double val = 0;
		try {
			val = Double.parseDouble(strVal.substring(0, length - 1));
		}
		catch (Exception e) {
			Cat.logError(e);
			return val;
		}
		
		if (unit == 'k') {
			val *= 1000;
		}
		else if (unit == 'K') {
			val *= 1024;
		}
		else if (unit == 'm') {
			val *= 1000000;
		}
		else if (unit == 'M') {
			val *= 1024 * 1024;
		}
		else if (unit == 'g') {
			val *= 1000000000;
		}
		else if (unit == 'G') {
			val *= 1024 * 1024 * 1024;
		}
		else if (unit == 't') {
			val *= 1000000000000.0;
		}
		else if (unit == 'T') {
			val *= 1024 * 1024 * 1024 * 1024;
		}
		else {
			val = 0;
		}

		return val;
	}
	
}
