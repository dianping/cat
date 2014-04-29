package com.dianping.cat.report.page.network.nettopology.model;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.network.nettopology.DomOp;

public class Anchor {
	private String m_name;

	private int m_x;

	private int m_y;

	public Anchor(Node node) {
		DomOp domOp = new DomOp(node);
		m_name = domOp.getAttribute("name");
		m_x = domOp.getAttrInt("x");
		m_y = domOp.getAttrInt("y");
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public int getX() {
		return m_x;
	}

	public void setX(int x) {
		m_x = x;
	}

	public int getY() {
		return m_y;
	}

	public void setY(int y) {
		m_y = y;
	}

}
