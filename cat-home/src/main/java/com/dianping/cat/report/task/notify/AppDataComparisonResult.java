package com.dianping.cat.report.task.notify;

import java.util.ArrayList;
import java.util.List;

public class AppDataComparisonResult {

	private String m_id;

	private List<AppDataComparisonItem> m_items = new ArrayList<AppDataComparisonItem>();

	public void addItem(String id, String command, double delay) {
		m_items.add(new AppDataComparisonItem(id, command, delay));
	}

	public List<AppDataComparisonItem> getItems() {
		return m_items;
	}

	public int getSize() {
		return m_items.size();
	}

	public void setId(String id) {
		m_id = id;
	}

	public String getId() {
		return m_id;
	}
	
	public class AppDataComparisonItem {

		private String m_id;

		private String m_command;

		private double m_delay;

		public AppDataComparisonItem(String id, String command, double delay) {
			m_id = id;
			m_command = command;
			m_delay = delay;
		}

		public String getId() {
			return m_id;
		}

		public String getCommand() {
			return m_command;
		}

		public double getDelay() {
			return m_delay;
		}
	}
}
