package com.dianping.cat.report.page.network.nettopology.model;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.network.nettopology.DomOp;

public class Switch {
	private String name;

	private int x;

	private int y;

	private int state;

	public Switch(Node node) {
		DomOp domOp = new DomOp(node);
		name = domOp.getAttribute("name");
		x = domOp.getAttrInt("x");
		y = domOp.getAttrInt("y");
		state = 1;
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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		if (state > this.state)
			this.state = state;
	}
}
