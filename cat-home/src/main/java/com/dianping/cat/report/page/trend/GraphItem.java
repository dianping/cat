package com.dianping.cat.report.page.trend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class GraphItem {

	private double[] m_ylable;

	private String m_titles;
	
	private String m_start;
	
	private int m_size;
	
	private List<String> m_subTitles = new ArrayList<String>();

	private List<double[]> m_values = new ArrayList<double[]>();

	private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public GraphItem() {
	}

	public GraphItem addSubTitle(String title) {
		m_subTitles.add(title);
		return this;
	}

	public GraphItem addValue(double[] value) {
		m_values.add(value);
		return this;
	}

	public String getJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String getStart() {
   	return m_start;
   }

	public void setStart(Date start) {
   	m_start = sdf.format(start);
   }

	public int getSize() {
   	return m_size;
   }

	public void setSize(int size) {
   	m_size = size;
   }

	public void setStart(String start) {
   	m_start = start;
   }

	public double[] getYlable() {
		return m_ylable;
	}

	public void setYlable(double[] ylable) {
		if (ylable == null) {
			m_ylable = new double[0];
		} else {
			m_ylable = Arrays.copyOf(ylable, ylable.length);
		}
	}

	public String getTitles() {
		return m_titles;
	}

	public void setTitles(String titles) {
		m_titles = titles;
	}

	public List<String> getSubTitles() {
		return m_subTitles;
	}

	public void setSubTitles(List<String> subTitles) {
		m_subTitles = subTitles;
	}

	public List<double[]> getValues() {
		return m_values;
	}

	public void setValues(List<double[]> values) {
		m_values = values;
	}
}
