package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.List;

public class PieChart {

	private List<Item> items = new ArrayList<Item>();

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public void addItem(Item item) {
		items.add(item);
		String title = item.getTitle();
		if (title.length() > 28) {
			title = title.substring(0, 12) + "..." + title.substring(title.length() - 12);
			item.setTitle(title);
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
