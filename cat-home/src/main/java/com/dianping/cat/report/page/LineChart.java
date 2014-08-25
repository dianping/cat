package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LineChart {

	private transient SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private int m_size;

	private long m_step;

	private String m_start;

	private List<String> m_subTitles = new ArrayList<String>();

	private String m_title;

	private String m_unit = "Value/分钟";

	private String m_id;

	private String m_htmlTitle;

	private List<double[]> m_values = new ArrayList<double[]>();

	private List<Double[]> m_valueObjects = new ArrayList<Double[]>();

	private List<Map<Long, Double>> m_datas = new ArrayList<Map<Long, Double>>();

	private double[] m_ylabel;

	private Double m_minYlabel = 0D;

	private Double m_maxYlabel;

	public LineChart() {
	}

	public LineChart add(String title, Double[] value) {
		m_subTitles.add(title);
		m_valueObjects.add(value);
		return this;
	}

	public LineChart add(String title, Map<Long, Double> data) {
		m_datas.add(data);
		m_subTitles.add(title);
		return this;
	}

	public LineChart addSubTitle(String title) {
		m_subTitles.add(title);
		return this;
	}

	public LineChart addValue(double[] value) {
		m_values.add(value);
		return this;
	}

	public List<Map<Long, Double>> getDatas() {
		return m_datas;
	}

	public String getHtmlTitle() {
		if (m_htmlTitle == null) {
			return m_title;
		} else {
			return m_htmlTitle;
		}
	}

	public String getUnit() {
		return m_unit;
	}

	public String getId() {
		return m_id;
	}

	public String getJsonString() {
		String json = new JsonBuilder().toJson(this);
		return json;
	}

	public int getSize() {
		return m_size;
	}

	public String getStart() {
		return m_start;
	}

	public long getStep() {
		return m_step;
	}

	public List<String> getSubTitles() {
		return m_subTitles;
	}

	public String getTitle() {
		return m_title;
	}

	public List<double[]> getValues() {
		return m_values;
	}

	public List<Double[]> getValueObjects() {
		return m_valueObjects;
	}

	public double[] getValues(int index) {
		int size = m_values.size();

		if (index > size) {
			return null;
		} else {
			return m_values.get(index);
		}
	}

	public double[] getYlable() {
		return m_ylabel;
	}

	public void setDatas(List<Map<Long, Double>> datas) {
		m_datas = datas;
	}

	public void setHtmlTitle(String htmlTitle) {
		m_htmlTitle = htmlTitle;
	}

	public void setId(String id) {
		m_id = id;
	}

	public LineChart setSize(int size) {
		m_size = size;
		return this;
	}

	public LineChart setStart(Date start) {
		m_start = m_sdf.format(start);
		return this;
	}

	public void setStep(long step) {
		m_step = step;
	}

	public LineChart setSubTitles(List<String> subTitles) {
		m_subTitles = subTitles;
		return this;
	}

	public LineChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public LineChart setUnit(String unit) {
		m_unit = unit;
		return this;
	}

	public LineChart setValues(List<double[]> values) {
		m_values = values;
		return this;
	}

	public Double getMinYlable() {
		return m_minYlabel;
	}

	public void setMinYlable(Double minYlable) {
		m_minYlabel = minYlable;
	}

	public Double getMaxYlabel() {
		return m_maxYlabel;
	}

	public void setMaxYlabel(Double maxYlabel) {
		m_maxYlabel = maxYlabel;
	}

	public LineChart setYlable(double[] ylable) {
		if (ylable == null) {
			m_ylabel = new double[0];
		} else {
			m_ylabel = Arrays.copyOf(ylable, ylable.length);
		}
		return this;
	}
}
