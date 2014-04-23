package com.dianping.cat.report.page.network.nettopology.model;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.network.nettopology.DomOp;

public class Interface {
	private String group;

	private String domain;

	private String key;

	private double in;

	private double out;

	private int state;

	public Interface(Node node) {
		DomOp domOp = new DomOp(node);
		group = domOp.getAttribute("group");
		domain = domOp.getAttribute("domain");
		key = domOp.getAttribute("key");
		state = 1;
	}

	public double getIn() {
		return in;
	}

	public void setIn(double in) {
		this.in = in;
	}

	public double getOut() {
		return out;
	}

	public void setOut(double out) {
		this.out = out;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
