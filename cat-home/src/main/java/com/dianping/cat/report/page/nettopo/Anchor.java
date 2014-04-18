package com.dianping.cat.report.page.nettopo;

import org.w3c.dom.Node;

public class Anchor {
	private String name;
	private int x;
	private int y;
	
	
	public Anchor(Node node) {
		DomOp domOp = new DomOp(node);
		name = domOp.getAttribute("name");
		x = domOp.getAttrInt("x");
		y = domOp.getAttrInt("y");
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
}
