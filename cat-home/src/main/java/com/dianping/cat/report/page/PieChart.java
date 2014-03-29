package com.dianping.cat.report.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PieChart {

	private List<Item> m_items = new ArrayList<Item>();

	private transient int MAX_SIZE = 10;

	public void addItems(List<Item> temps) {
		Collections.sort(temps, new ItemCompartor());
		int size = temps.size();

		if (size <= MAX_SIZE) {
			m_items = temps;
		} else {
			for (int i = 0; i < MAX_SIZE; i++) {
				m_items.add(temps.get(i));
			}
			Item item = new Item().setTitle("Other");

			double sum = 0;
			for (int i = MAX_SIZE; i < size; i++) {
				Item temp = temps.get(i);

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

	public static class Item {
		private String m_title;

		private double m_number;

		public double getNumber() {
			return m_number;
		}

		public String getTitle() {
			return m_title;
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
