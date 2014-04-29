package com.dianping.cat.report.page.network.nettopology.model;

import java.util.ArrayList;

import org.w3c.dom.Node;

import com.dianping.cat.report.page.network.nettopology.DomOp;

public class Connection {
	private String m_firstName;

	private String m_secondName;

	private transient ArrayList<Interface> m_firstData;

	private transient ArrayList<Interface> m_secondData;

	private double m_firstInSum;

	private double m_firstOutSum;

	private int m_firstState;

	private double m_secondInSum;

	private double m_secondOutSum;

	private int m_secondState;

	public Connection(Node node) {
		DomOp domOp = new DomOp(node);
		m_firstName = domOp.getAttribute("first");
		m_secondName = domOp.getAttribute("second");
		m_firstData = new ArrayList<Interface>();
		m_secondData = new ArrayList<Interface>();
		m_firstState = 1;
		m_secondState = 1;

		for (Node n : domOp.getChildNodes("firstdata")) {
			for (Node interfaceNode : (new DomOp(n)).getChildNodes("interface")) {
				m_firstData.add(new Interface(interfaceNode));
			}
		}

		for (Node n : domOp.getChildNodes("seconddata")) {
			for (Node interfaceNode : (new DomOp(n)).getChildNodes("interface")) {
				m_secondData.add(new Interface(interfaceNode));
			}
		}
	}

	public String getFirstName() {
		return m_firstName;
	}

	public void setFirstName(String firstName) {
		m_firstName = firstName;
	}

	public String getSecondName() {
		return m_secondName;
	}

	public void setSecondName(String secondName) {
		m_secondName = secondName;
	}

	public ArrayList<Interface> getFirstData() {
		return m_firstData;
	}

	public void setFirstData(ArrayList<Interface> firstData) {
		m_firstData = firstData;
	}

	public ArrayList<Interface> getSecondData() {
		return m_secondData;
	}

	public void setSecondData(ArrayList<Interface> secondData) {
		m_secondData = secondData;
	}

	public double getFirstInSum() {
		return m_firstInSum;
	}

	public void setFirstInSum(double firstInSum) {
		m_firstInSum = firstInSum;
	}

	public double getFirstOutSum() {
		return m_firstOutSum;
	}

	public void setFirstOutSum(double firstOutSum) {
		m_firstOutSum = firstOutSum;
	}

	public int getFirstState() {
		return m_firstState;
	}

	public void setFirstState(int firstState) {
		m_firstState = firstState;
	}

	public double getSecondInSum() {
		return m_secondInSum;
	}

	public void setSecondInSum(double secondInSum) {
		m_secondInSum = secondInSum;
	}

	public double getSecondOutSum() {
		return m_secondOutSum;
	}

	public void setSecondOutSum(double secondOutSum) {
		m_secondOutSum = secondOutSum;
	}

	public int getSecondState() {
		return m_secondState;
	}

	public void setSecondState(int secondState) {
		m_secondState = secondState;
	}

}
