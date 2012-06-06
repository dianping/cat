package com.dianping.cat.report.page.trend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class GraphItem {

	private double[] ylable;

	private String titles;
	
	private String start;
	
	private int size;
	
	private List<String> subTitles = new ArrayList<String>();

	private List<double[]> values = new ArrayList<double[]>();

	private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	public GraphItem() {
	}

	public GraphItem addSubTitle(String title) {
		subTitles.add(title);
		return this;
	}

	public GraphItem addValue(double[] value) {
		values.add(value);
		return this;
	}

	public String getJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String getStart() {
   	return start;
   }

	public void setStart(Date start) {
   	this.start = sdf.format(start);
   }

	public int getSize() {
   	return size;
   }

	public void setSize(int size) {
   	this.size = size;
   }

	public void setStart(String start) {
   	this.start = start;
   }

	public double[] getYlable() {
		return ylable;
	}

	public void setYlable(double[] ylable) {
		this.ylable = ylable;
	}

	public String getTitles() {
		return titles;
	}

	public void setTitles(String titles) {
		this.titles = titles;
	}

	public List<String> getSubTitles() {
		return subTitles;
	}

	public void setSubTitles(List<String> subTitles) {
		this.subTitles = subTitles;
	}

	public List<double[]> getValues() {
		return values;
	}

	public void setValues(List<double[]> values) {
		this.values = values;
	}
}
