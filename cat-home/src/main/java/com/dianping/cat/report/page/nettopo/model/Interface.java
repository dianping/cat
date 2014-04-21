package com.dianping.cat.report.page.nettopo.model;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.nettopo.DomOp;

public class Interface {
	private String group;

	private String domain;

	private String key;

	private double inThresholdWarnning;

	private double inThresholdSerious;

	private double outThresholdWarnning;

	private double outThresholdSerious;

	private double in;

	private double out;

	private int state;

	public Interface(Node node) {
		DomOp domOp = new DomOp(node);
		group = domOp.getAttribute("group");
		domain = domOp.getAttribute("domain");
		key = domOp.getAttribute("key");
		inThresholdWarnning = domOp.getAttrDouble("inwarnning");
		inThresholdSerious = domOp.getAttrDouble("inserious");
		outThresholdWarnning = domOp.getAttrDouble("outwarnning");
		outThresholdSerious = domOp.getAttrDouble("outserious");
		state = 1;
	}

	public double getIn() {
		return in;
	}

	public void setIn(double in) {
		this.in = in;
		if (inThresholdWarnning != 0 && state < 2 && in >= inThresholdWarnning) {
			state = 2;
		}
		if (inThresholdSerious != 0 && state < 3 && in >= inThresholdSerious) {
			state = 3;
		}
		if (outThresholdWarnning != 0 && state < 2 && out >= outThresholdWarnning) {
			state = 2;
		}
		if (outThresholdSerious != 0 && state < 3 && out >= outThresholdSerious) {
			state = 3;
		}
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

	public double getInThresholdWarnning() {
		return inThresholdWarnning;
	}

	public void setInThresholdWarnning(double inThresholdWarnning) {
		this.inThresholdWarnning = inThresholdWarnning;
	}

	public double getInThresholdSerious() {
		return inThresholdSerious;
	}

	public void setInThresholdSerious(double inThresholdSerious) {
		this.inThresholdSerious = inThresholdSerious;
	}

	public double getOutThresholdWarnning() {
		return outThresholdWarnning;
	}

	public void setOutThresholdWarnning(double outThresholdWarnning) {
		this.outThresholdWarnning = outThresholdWarnning;
	}

	public double getOutThresholdSerious() {
		return outThresholdSerious;
	}

	public void setOutThresholdSerious(double outThresholdSerious) {
		this.outThresholdSerious = outThresholdSerious;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
