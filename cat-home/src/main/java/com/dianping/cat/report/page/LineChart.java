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
	
	private String m_id;

	private String m_htmlTitle;

	private List<double[]> m_values = new ArrayList<double[]>();

	private List<Map<Long, Double>> m_datas = new ArrayList<Map<Long, Double>>();

	private double[] m_ylable;

	public LineChart() {
	}

	public LineChart add(String title, Map<Long, Double> data) {
		m_datas.add(data);
		m_subTitles.add(title);
		return this;
	}

	public LineChart add(String title, double[] value) {
		m_subTitles.add(title);
		m_values.add(value);
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

	public String getJsonString() {
		return new JsonBuilder().toJson(this);
	}

	public int getSize() {
		return m_size;
	}

	public String getStart() {
		return m_start;
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

	public double[] getYlable() {
		return m_ylable;
	}

	public LineChart setSize(int size) {
		m_size = size;
		return this;
	}

	public LineChart setStart(Date start) {
		m_start = m_sdf.format(start);
		return this;
	}

	public LineChart setSubTitles(List<String> subTitles) {
		m_subTitles = subTitles;
		return this;
	}

	public LineChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public LineChart setValues(List<double[]> values) {
		m_values = values;
		return this;
	}

	public LineChart setYlable(double[] ylable) {
		if (ylable == null) {
			m_ylable = new double[0];
		} else {
			m_ylable = Arrays.copyOf(ylable, ylable.length);
		}
		return this;
	}

	public long getStep() {
		return m_step;
	}

	public void setStep(long step) {
		m_step = step;
	}

	public double[] getValues(int index) {
		int size = m_values.size();

		if (index > size) {
			return null;
		} else {
			return m_values.get(index);
		}
	}

	public List<Map<Long, Double>> getDatas() {
		return m_datas;
	}

	public void setDatas(List<Map<Long, Double>> datas) {
		m_datas = datas;
	}

	public String getHtmlTitle() {
		if (m_htmlTitle == null) {
			return m_title;
		} else {
			return m_htmlTitle;
		}
	}

	public void setHtmlTitle(String htmlTitle) {
		m_htmlTitle = htmlTitle;
	}

	public String getId() {
   	return m_id;
   }

	public void setId(String id) {
   	m_id = id;
   }
	
}
