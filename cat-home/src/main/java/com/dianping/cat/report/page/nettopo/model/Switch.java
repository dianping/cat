package com.dianping.cat.report.page.nettopo.model;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.nettopo.DomOp;

public class Switch {
	private String name;

	private int x;

	private int y;

	private String desc;

	private int state;

	public Switch(Node node) {
		DomOp domOp = new DomOp(node);
		name = domOp.getAttribute("name");
		desc = domOp.getAttribute("desc");
		x = domOp.getAttrInt("x");
		y = domOp.getAttrInt("y");
		state = 1;
	}

	public void appendToJson(StringBuilder sbJson) {
		sbJson.append("'");
		sbJson.append(name);
		sbJson.append("':{'x':");
		sbJson.append(Integer.toString(x));
		sbJson.append(",'y':");
		sbJson.append(Integer.toString(y));
		sbJson.append(",'state':");
		sbJson.append(Integer.toString(state));
		sbJson.append("},");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		if (state > this.state)
			this.state = state;
	}
}
