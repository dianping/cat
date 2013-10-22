package com.dianping.cat.report.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;

public class PieChart {

	private List<Item> items = new ArrayList<Item>();

	public PieChart() {
	}

	public List<Item> getItems() {
		return items;
	}

	public String getJsonString() {
		return new Gson().toJson(this);
	}

	public void addItems(List<Item> items) {
		Collections.sort(items, new ItemCompartor());
		int size = items.size();
		int maxSize = 10;

		if (size <= maxSize) {
			this.items = items;
		} else {
			for (int i = 0; i < maxSize; i++) {
				this.items.add(items.get(i));
			}
			Item item = new Item();
			item.setTitle("Other");
			double sum = 0;
			for (int i = maxSize; i < size; i++) {
				Item temp = items.get(i);
				sum += temp.getNumber();
			}
			item.setNumber(sum);
			this.items.add(item);
		}
	}

	public static class Item {
		private String title;

		private double number;

		public double getNumber() {
			return number;
		}

		public String getTitle() {
			return title;
		}

		public Item setNumber(double number) {
			this.number = number;
			return this;
		}

		public Item setTitle(String title) {
			this.title = title;
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
