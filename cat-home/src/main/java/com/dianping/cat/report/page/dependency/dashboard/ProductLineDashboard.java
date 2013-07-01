package com.dianping.cat.report.page.dependency.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.google.gson.Gson;

public class ProductLineDashboard {
	
	private String id;
	
	private String type = GraphConstrant.PROJECT;
	
	private int status;
	
	private String des;

	private List<TopologyNode> nodes = new ArrayList<TopologyNode>();

	public ProductLineDashboard(String productLine) {
		id = productLine;
	}

	public ProductLineDashboard addNode(TopologyNode node) {
		nodes.add(node);

		return this;
	}
	
	public List<TopologyNode> getNodes() {
   	return nodes;
   }

	public String getId() {
   	return id;
   }

	public void setId(String id) {
   	this.id = id;
   }

	public int getStatus() {
   	return status;
   }

	public void setStatus(int status) {
   	this.status = status;
   }

	public String getDes() {
   	return des;
   }

	public String getType() {
   	return type;
   }

	public void setType(String type) {
   	this.type = type;
   }

	public void setDes(String des) {
   	this.des = des;
   }

	public List<TopologyNode> getPoints() {
   	return nodes;
   }

	public void setPoints(List<TopologyNode> points) {
   	this.nodes = points;
   }

	public String toJson() {
		String str = new Gson().toJson(this);
		str = str.replaceAll("\"m_", "\"");
		str = str.replaceAll("\"nodes\"", "\"points\"");
		return str;
	}
}
