package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class LineChart {

	private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private int size;

	private long step;

	private String start;

	private List<String> subTitles = new ArrayList<String>();

	private String title;

	private List<double[]> values = new ArrayList<double[]>();

	private List<Map<Long, Double>> datas = new ArrayList<Map<Long, Double>>();

	private double[] ylable;

	public LineChart() {
	}

	public LineChart add(String title, Map<Long, Double> data) {
		this.datas.add(data);
		this.subTitles.add(title);
		return this;
	}

	public LineChart add(String title, double[] value) {
		this.subTitles.add(title);
		this.values.add(value);
		return this;
	}

	public LineChart addSubTitle(String title) {
		this.subTitles.add(title);
		return this;
	}

	public LineChart addValue(double[] value) {
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

	public String getTitle() {
		return this.title;
	}

	public List<double[]> getValues() {
		return this.values;
	}

	public double[] getYlable() {
		return this.ylable;
	}

	public LineChart setSize(int size) {
		this.size = size;
		return this;
	}

	public LineChart setStart(Date start) {
		this.start = sdf.format(start);
		return this;
	}

	public LineChart setSubTitles(List<String> subTitles) {
		this.subTitles = subTitles;
		return this;
	}

	public LineChart setTitle(String title) {
		this.title = title;
		return this;
	}

	public LineChart setValues(List<double[]> values) {
		this.values = values;
		return this;
	}

	public LineChart setYlable(double[] ylable) {
		if (ylable == null) {
			this.ylable = new double[0];
		} else {
			this.ylable = Arrays.copyOf(ylable, ylable.length);
		}
		return this;
	}

	public long getStep() {
		return step;
	}

	public void setStep(long step) {
		this.step = step;
	}

	public double[] getValues(int index) {
		int size = values.size();

		if (index > size) {
			return null;
		} else {
			return values.get(index);
		}
	}

	public List<Map<Long, Double>> getDatas() {
   	return datas;
   }

	public void setDatas(List<Map<Long, Double>> datas) {
   	this.datas = datas;
   }
	
}
