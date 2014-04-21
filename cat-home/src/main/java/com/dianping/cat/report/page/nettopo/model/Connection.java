package com.dianping.cat.report.page.nettopo.model;

import java.util.ArrayList;

import org.unidal.tuple.Pair;
import org.w3c.dom.Node;

import com.dianping.cat.report.page.nettopo.DomOp;

public class Connection {
	private String first;

	private String second;

	private ArrayList<Interface> firstData;

	private ArrayList<Interface> secondData;

	private int firstState;

	private int secondState;

	public Connection(Node node) {
		DomOp domOp = new DomOp(node);
		first = domOp.getAttribute("first");
		second = domOp.getAttribute("second");
		firstData = new ArrayList<Interface>();
		secondData = new ArrayList<Interface>();

		for (Node n : domOp.getChildNodes("firstdata")) {
			for (Node interfaceNode : (new DomOp(n)).getChildNodes("interface")) {
				firstData.add(new Interface(interfaceNode));
			}
		}

		for (Node n : domOp.getChildNodes("seconddata")) {
			for (Node interfaceNode : (new DomOp(n)).getChildNodes("interface")) {
				secondData.add(new Interface(interfaceNode));
			}
		}
	}

	public Pair<String, Integer> setFirstState() {
		int state = 1;
		int st;
		for (Interface in : firstData) {
			st = in.getState();
			if (st > state)
				state = st;
		}

		this.firstState = state;

		return new Pair<String, Integer>(first, state);
	}

	public Pair<String, Integer> setSecondState() {
		int state = 1;
		int st;
		for (Interface in : secondData) {
			st = in.getState();
			if (st > state)
				state = st;
		}

		this.secondState = state;

		return new Pair<String, Integer>(second, state);
	}

	public void appendToJson(StringBuilder sbJson) {
		double in, out;

		sbJson.append("[");

		sbJson.append("['");
		sbJson.append(first);
		sbJson.append("',");
		in = 0;
		for (Interface interface_ : firstData) {
			in += interface_.getIn();
		}
		sbJson.append(Double.toString(in));
		sbJson.append(",");
		out = 0;
		for (Interface interface_ : firstData) {
			out += interface_.getOut();
		}
		sbJson.append(Double.toString(out));
		sbJson.append(",");
		sbJson.append(Integer.toString(firstState));
		sbJson.append(",");
		sbJson.append("],");

		sbJson.append("['");
		sbJson.append(second);
		sbJson.append("',");
		in = 0;
		for (Interface interface_ : secondData) {
			in += interface_.getIn();
		}
		sbJson.append(Double.toString(in));
		sbJson.append(",");
		out = 0;
		for (Interface interface_ : secondData) {
			out += interface_.getOut();
		}
		sbJson.append(Double.toString(out));
		sbJson.append(",");
		sbJson.append(Integer.toString(secondState));
		sbJson.append(",");
		sbJson.append("],");

		sbJson.append("],");
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public ArrayList<Interface> getFirstData() {
		return firstData;
	}

	public void setFirstData(ArrayList<Interface> firstData) {
		this.firstData = firstData;
	}

	public ArrayList<Interface> getSecondData() {
		return secondData;
	}

	public void setSecondData(ArrayList<Interface> secondData) {
		this.secondData = secondData;
	}

	public void addInterfaceToFirst(Interface e) {
		firstData.add(e);
	}

	public void addInterfaceToSecond(Interface e) {
		secondData.add(e);
	}

	public int getFirstState() {
		return firstState;
	}

	public void setFirstState(int firstState) {
		this.firstState = firstState;
	}

	public int getSecondState() {
		return secondState;
	}

	public void setSecondState(int secondState) {
		this.secondState = secondState;
	}
}
