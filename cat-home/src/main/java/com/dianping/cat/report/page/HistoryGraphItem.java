package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class HistoryGraphItem {

	private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private int size;

	private String start;

	private List<String> subTitles = new ArrayList<String>();

	private String titles;

	private List<double[]> values = new ArrayList<double[]>();

	private double[] ylable;

	public HistoryGraphItem() {
	}

	public HistoryGraphItem addSubTitle(String title) {
		this.subTitles.add(title);
		return this;
	}

	public HistoryGraphItem addValue(double[] value) {
		this.values.add(value);
		return this;
	}

	public String getJsonString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public int getSize() {
		return this.size;
	}

	public String getStart() {
		return this.start;
	}

	public List<String> getSubTitles() {
		return this.subTitles;
	}

	public String getTitles() {
		return this.titles;
	}

	public List<double[]> getValues() {
		return this.values;
	}

	public double[] getYlable() {
		return this.ylable;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setStart(Date start) {
		this.start = sdf.format(start);
	}

	public void setStart(String start) {
		this.start = start;
	}

	public void setSubTitles(List<String> subTitles) {
		this.subTitles = subTitles;
	}

	public void setTitles(String titles) {
		this.titles = titles;
	}

	public void setValues(List<double[]> values) {
		this.values = values;
	}

	public void setYlable(double[] ylable) {
		if (ylable == null) {
			this.ylable = new double[0];
		} else {
			this.ylable = Arrays.copyOf(ylable, ylable.length);
		}
	}
}
