package com.dianping.cat.report.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PieChart {

	private List<Item> items = new ArrayList<Item>();

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> temps) {
		Collections.sort(temps, new ItemCompartor());
		int size = temps.size();

		if (size <= 10) {
			this.items = temps;
		} else {
			this.items = temps.subList(0, 10);

			Item item = new Item();
			item.setTitle("Other");
			double sum = 0;
			for (int i = 10; i < size; i++) {
				Item temp = temps.get(i);
				sum += temp.getNumber();
			}
			item.setNumber(sum);
			items.add(item);
		}

		for (Item item : items) {
			String title = item.getTitle();
			if (title.length() > 28) {
				title = title.substring(0, 11) + "..." + title.substring(title.length() - 11);
				item.setTitle(title);
			}
		}
	}

	public static class ItemCompartor implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			return (int) (o2.getNumber() - o1.getNumber());
		}
	}

	public static class Item {
		private String title;

		private double number;

		public String getTitle() {
			return title;
		}

		public Item setTitle(String title) {
			this.title = title;
			return this;
		}

		public double getNumber() {
			return number;
		}

		public Item setNumber(double number) {
			this.number = number;
			return this;
		}
	}
}
