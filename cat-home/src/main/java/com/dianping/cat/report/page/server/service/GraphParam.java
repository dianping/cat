package com.dianping.cat.report.page.server.service;

import java.util.Arrays;
import java.util.List;

public class GraphParam {
	private String m_name;

	private String m_category;

	private String m_graphName;

	private String m_view;

	private List<String> m_endPoints;

	private List<String> m_measurements;

	public String getCategory() {
		return m_category;
	}

	public String getGraphName() {
		return m_graphName;
	}

	public String getName() {
		return m_name;
	}

	public List<String> getEndPoints() {
		return m_endPoints;
	}

	public String getView() {
		return m_view;
	}

	public List<String> getMeasurements() {
		return m_measurements;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setGraphName(String graphName) {
		m_graphName = graphName;
	}

	public void setEndPoints(String endPoints) {
		String[] ends = endPoints.split(",[ ]*");
		m_endPoints = Arrays.asList(ends);
	}

	public void setMeasurements(String measurements) {
		String[] measures = measurements.split(",[ ]*");
		m_measurements = Arrays.asList(measures);
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setView(String view) {
		m_view = view;
	}
}
