package com.dianping.cat.report.page.nettopo;

import java.util.ArrayList;
import org.w3c.dom.Node;

public class Connection {
	private String first;
	private String second;
	private ArrayList<Interface> firstData;
	private ArrayList<Interface> secondData;
	
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
}
