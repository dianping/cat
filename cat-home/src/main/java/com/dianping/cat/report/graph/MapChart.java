
package com.dianping.cat.report.graph;

import java.util.ArrayList;
import java.util.List;

public class MapChart {

	private String m_title;

	private String m_subTitle;

	private int m_min;

	private int m_max;

	private List<Item> m_dataSeries = new ArrayList<Item>();
	
	private String m_data;
	
	public String getData() {
		return m_data;
	}
	
	public void setData(String data) {
		m_data = data;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getSubTitle() {
		return m_subTitle;
	}

	public void setSubTitle(String subTitle) {
		m_subTitle = subTitle;
	}

	public int getMin() {
		return m_min;
	}

	public void setMin(int min) {
		m_min = min;
	}

	public int getMax() {
		return m_max;
	}

	public void setMax(int max) {
		m_max = max;
	}

	public void setDataSeries(List<Item> items) {
		m_dataSeries = items;
	}

	public List<Item> getDataSeries() {
		return m_dataSeries;
	}

	public static class Item {

		private String m_name;

		private double m_value;

		public Item(String name, double value) {
			m_name = name;
			m_value = value;
		}

		public String getName() {
			return m_name;
		}

		public void setName(String name) {
			m_name = name;
		}

		public double getValue() {
			return m_value;
		}

		public void setValue(Double value) {
			m_value = value;
		}
	}
}