package com.dianping.cat.report.page.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;

public class DisplayMatrix {
	private Map<String, MatrixItem> m_matrix = new TreeMap<String, MatrixItem>();

	private String m_sortBy = "name";

	public DisplayMatrix(MatrixReport report) {
		if (report == null) {
			return;
		}
		Map<String, Matrix> reportMatrixs = report.getMatrixs();
		for (Matrix matrix : reportMatrixs.values()) {
			String key = matrix.getName();
			MatrixItem item = m_matrix.get(key);
			if (item == null) {
				item = new MatrixItem();
				item.setBaseInfo(matrix);
				item.setCacheInfo(matrix);
				item.setCallInfo(matrix);
				item.setSQLInfo(matrix);
				m_matrix.put(key, item);
			} else {
				Cat.getProducer().logError(new RuntimeException("Inter errer in matrix analyers!"));
			}
		}
	}

	public DisplayMatrix setSortBy(String sort) {
		if (sort != null) {
			m_sortBy = sort;
		}
		return this;
	}

	public List<MatrixItem> getMatrixs() {
		List<MatrixItem> result = new ArrayList<MatrixItem>(m_matrix.values());
		Collections.sort(result, new MatrixItemCompartor(m_sortBy));
		return result;
	}

	static class MatrixItemCompartor implements Comparator<MatrixItem> {
		private String m_sort;

		public MatrixItemCompartor(String sort) {
			m_sort = sort;
		}

		@Override
		public int compare(MatrixItem o1, MatrixItem o2) {
			if (m_sort.equalsIgnoreCase("name")) {
				if (o2.getType().equals(o1.getType())) {
					return o1.getName().compareTo(o2.getName());
				} else {
					return o1.getType().compareTo(o2.getType());
				}
			}
			if (m_sort.equalsIgnoreCase("count")) {
				return o2.getCount() - o1.getCount();
			}
			if (m_sort.equalsIgnoreCase("time")) {
				return (int) (o2.getAvg() * 100 - o1.getAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("CallMinCount")) {
				return o2.getCallMin() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("CallMaxCount")) {
				return o2.getCallMax() - o1.getCacheMax();
			}
			if (m_sort.equalsIgnoreCase("CallAvgCount")) {
				return (int) (o2.getCallAvg() * 100 - o1.getCallAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("CallAvgTotalTime")) {
				return o2.getCallTime() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("callTimePercent")) {
				return (int) (o2.getCallTimePercent() * 100 - o1.getCallTimePercent() * 100);
			}
			if (m_sort.equalsIgnoreCase("SqlMinCount")) {
				return o2.getSqlMin() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("SqlMaxCount")) {
				return o2.getSqlMax() - o1.getCacheMax();
			}
			if (m_sort.equalsIgnoreCase("SqlAvgCount")) {
				return (int) (o2.getSqlAvg() * 100 - o1.getSqlAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("SqlAvgTotalTime")) {
				return o2.getSqlTime() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("SqlTimePercent")) {
				return (int) (o2.getSqlTimePercent() * 100 - o1.getSqlTimePercent() * 100);
			}
			if (m_sort.equalsIgnoreCase("CacheMinCount")) {
				return o2.getCacheMin() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("CacheMaxCount")) {
				return o2.getCacheMax() - o1.getCacheMax();
			}
			if (m_sort.equalsIgnoreCase("CacheAvgCount")) {
				return (int) (o2.getCacheAvg() * 100 - o1.getCacheAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("CacheAvgTotalTime")) {
				return o2.getCacheTime() - o1.getCacheMin();
			}
			if (m_sort.equalsIgnoreCase("CacheTimePercent")) {
				return (int) (o2.getCacheTimePercent() * 100 - o1.getCacheTimePercent() * 100);
			}
			return 0;
		}
	}

	public static class MatrixItem {
		private String m_type;

		private String m_name;

		private int m_count;

		private double m_avg;

		private String m_url;

		private int m_callMin;

		private int m_callMax;

		private double m_callAvg;

		private int m_callTime;

		private double m_callTimePercent;

		private int m_sqlMin;

		private int m_sqlMax;

		private double m_sqlAvg;

		private int m_sqlTime;

		private double m_sqlTimePercent;

		private int m_cacheMin;

		private int m_cacheMax;

		private double m_cacheAvg;

		private int m_cacheTime;

		private double m_cacheTimePercent;

		public void setBaseInfo(Matrix matrix) {
			m_type = matrix.getType();
			m_name = matrix.getName();
			m_count = matrix.getCount();
			if (matrix.getCount() > 0) {
				m_avg = (double) matrix.getTotalTime() / (double) matrix.getCount() / (double) 1000;
			}
			m_url = matrix.getUrl();
		}

		public void setCacheInfo(Matrix matrix) {

			Ratio ratio = matrix.getRatios().get("Cache");
			m_cacheMin = ratio.getMin();
			m_cacheMax = ratio.getMax();
			if (matrix.getCount() > 0) {
				m_cacheAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			m_cacheTime = (int) (ratio.getTotalTime() / 1000);
			if (matrix.getTotalTime() > 0) {
				m_cacheTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}

		public void setSQLInfo(Matrix matrix) {
			Ratio ratio = matrix.getRatios().get("SQL");
			m_sqlMin = ratio.getMin();
			m_sqlMax = ratio.getMax();
			if (matrix.getCount() > 0) {
				m_sqlAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			m_sqlTime = (int) (ratio.getTotalTime() / 1000);
			if (matrix.getTotalTime() > 0) {
				m_sqlTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}

		public void setCallInfo(Matrix matrix) {
			Ratio ratio = matrix.getRatios().get("Call");
			m_callMin = ratio.getMin();
			m_callMax = ratio.getMax();
			if (matrix.getCount() > 0) {
				m_callAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			m_callTime = (int) (ratio.getTotalTime() / 1000);
			if (matrix.getTotalTime() > 0) {
				m_callTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}

		public double getCacheAvg() {
			return m_cacheAvg;
		}

		public int getCacheMax() {
			return m_cacheMax;
		}

		public int getCacheMin() {
			return m_cacheMin;
		}

		public int getCacheTime() {
			return m_cacheTime;
		}

		public double getCacheTimePercent() {
			return m_cacheTimePercent;
		}

		public double getCallAvg() {
			return m_callAvg;
		}

		public int getCallMax() {
			return m_callMax;
		}

		public int getCallMin() {
			return m_callMin;
		}

		public int getCallTime() {
			return m_callTime;
		}

		public double getCallTimePercent() {
			return m_callTimePercent;
		}

		public String getName() {
			return m_name;
		}

		public double getSqlAvg() {
			return m_sqlAvg;
		}

		public int getSqlMax() {
			return m_sqlMax;
		}

		public int getSqlMin() {
			return m_sqlMin;
		}

		public int getSqlTime() {
			return m_sqlTime;
		}

		public double getSqlTimePercent() {
			return m_sqlTimePercent;
		}

		public String getType() {
			return m_type;
		}

		public int getCount() {
			return m_count;
		}

		public double getAvg() {
			return m_avg;
		}

		public String getUrl() {
			return m_url;
		}
	}
}
