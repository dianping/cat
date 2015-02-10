package com.dianping.cat.report.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dianping.cat.helper.JsonBuilder;

public class PieChart {

	private String m_title;

	private List<Item> m_items = new ArrayList<Item>();

	private transient int m_size = 30;

	public void addItems(List<Item> items) {
		Collections.sort(items, new ItemCompartor());
		int size = items.size();

		if (size <= m_size) {
			m_items = items;
		} else {
			for (int i = 0; i < m_size; i++) {
				m_items.add(items.get(i));
			}
			Item item = new Item().setTitle("Other");

			double sum = 0;
			for (int i = m_size; i < size; i++) {
				Item temp = items.get(i);

				sum += temp.getNumber();
			}
			m_items.add(item.setNumber(sum));
		}
	}

	public List<Item> getItems() {
		return m_items;
	}

	public String getJsonString() {
		return new JsonBuilder().toJson(this);
	}

	public String getTitle() {
		return m_title;
	}

	public PieChart setMaxSize(int size) {
		m_size = size;
		return this;
	}

	public PieChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public static class Item {
		private int m_id;
		
		private String m_title;

		private double m_number;
		
		public int getId() {
      	return m_id;
      }

		public double getNumber() {
			return m_number;
		}

		public String getTitle() {
			return m_title;
		}

		public void setId(int id) {
      	m_id = id;
      }

		public Item setNumber(double number) {
			m_number = number;
			return this;
		}

		public Item setTitle(String title) {
			m_title = title;
			return this;
		}
	}

	public static class ItemCompartor implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			return (int) (o2.getNumber() - o1.getNumber());
		}
	}
}
