package com.dianping.cat.report.page.dependency.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;

public class ProductLineDashboard {

	private String m_id;

	private String m_type = GraphConstrant.PROJECT;

	private int m_status;

	private String m_des;

	private List<TopologyNode> m_points = new ArrayList<TopologyNode>();

	public ProductLineDashboard(String productLine) {
		m_id = productLine;
	}

	public ProductLineDashboard addPoint(TopologyNode node) {
		m_points.add(node);

		return this;
	}

	public List<TopologyNode> getPoints() {
		return m_points;
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public int getStatus() {
		return m_status;
	}

	public void setStatus(int status) {
		m_status = status;
	}

	public String getDes() {
		return m_des;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setDes(String des) {
		m_des = des;
	}

	public void setPoints(List<TopologyNode> points) {
		m_points = points;
	}

	public String toJson() {
		return new JsonBuilder().toJson(this);
	}
}
