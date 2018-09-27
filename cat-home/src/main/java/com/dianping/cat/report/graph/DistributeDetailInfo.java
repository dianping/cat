package com.dianping.cat.report.graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class DistributeDetailInfo {

	private List<DistributeDetail> m_items = new LinkedList<DistributeDetail>();

	public void add(DistributeDetail item) {
		m_items.add(item);
	}

	public List<DistributeDetail> getItems() {
		return m_items;
	}

	public List<DistributeDetail> getRequestSortedItems() {
		Collections.sort(m_items, new Comparator<DistributeDetail>() {
			public int compare(DistributeDetail o1, DistributeDetail o2) {
				return (int) (o2.getRequestSum() - o1.getRequestSum());
			}
		});

		return m_items;
	}

	public List<DistributeDetail> getDelaySortedItems() {
		Collections.sort(m_items, new Comparator<DistributeDetail>() {
			public int compare(DistributeDetail o1, DistributeDetail o2) {
				return (int) (o2.getDelayAvg() - o1.getDelayAvg());
			}
		});

		return m_items;
	}

	public void setItems(List<DistributeDetail> items) {
		m_items = items;
	}

	public static class DistributeDetail {
		private int m_id;

		private String m_title;

		private double m_requestSum;

		private double m_ratio;

		private double m_delayAvg;

		public int getId() {
			return m_id;
		}

		public double getRequestSum() {
			return m_requestSum;
		}

		public double getRatio() {
			return m_ratio;
		}

		public String getTitle() {
			return m_title;
		}

		public DistributeDetail setId(int id) {
			m_id = id;
			return this;
		}

		public double getDelayAvg() {
			return m_delayAvg;
		}

		public void setDelayAvg(double delayAvg) {
			m_delayAvg = delayAvg;
		}

		public DistributeDetail setRequestSum(double requestSum) {
			m_requestSum = requestSum;
			return this;
		}

		public DistributeDetail setRatio(double ratio) {
			m_ratio = ratio;
			return this;
		}

		public DistributeDetail setTitle(String title) {
			m_title = title;
			return this;
		}
	}

}
