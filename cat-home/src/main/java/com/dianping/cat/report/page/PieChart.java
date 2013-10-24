package com.dianping.cat.report.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;

public class PieChart {

	private List<Item> items = new ArrayList<Item>();

	private transient int MAX_SIZE = 10;

	public List<Item> getItems() {
		return items;
	}

	public String getJsonString() {
		return new Gson().toJson(this);
	}

	public void addItems(List<Item> temps) {
		Collections.sort(temps, new ItemCompartor());
		int size = temps.size();

		if (size <= MAX_SIZE) {
			this.items = temps;
		} else {
			for (int i = 0; i < MAX_SIZE; i++) {
				this.items.add(temps.get(i));
			}
			Item item = new Item().setTitle("Other");

			double sum = 0;
			for (int i = MAX_SIZE; i < size; i++) {
				Item temp = temps.get(i);

				sum += temp.getNumber();
			}
			this.items.add(item.setNumber(sum));
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
