package com.dianping.cat.report.page.trend;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class GraphItem {

	private String[] xlabel;

	private double[] ylable;

	private String titles;

	private List<String> subTitles = new ArrayList<String>();

	private List<double[]> values = new ArrayList<double[]>();

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

	public String[] getXlabel() {
		return xlabel;
	}

	public void setXlabel(String[] xlabel) {
		this.xlabel = xlabel;
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
