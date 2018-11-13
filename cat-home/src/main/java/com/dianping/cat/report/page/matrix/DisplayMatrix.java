/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
				Cat.logError(new RuntimeException("Inter errer in matrix analyers!"));
			}
		}
	}

	public List<MatrixItem> getMatrixs() {
		List<MatrixItem> result = new ArrayList<MatrixItem>(m_matrix.values());
		Collections.sort(result, new MatrixItemCompartor(m_sortBy));
		return result;
	}

	public DisplayMatrix setSortBy(String sort) {
		if (sort != null) {
			m_sortBy = sort;
		}
		return this;
	}

	public static class MatrixItem {
		private double m_avg;

		private double m_cacheAvg;

		private int m_cacheMax;

		private int m_cacheMin;

		private int m_cacheTime;

		private double m_cacheTimePercent;

		private double m_callAvg;

		private int m_callMax;

		private int m_callMin;

		private int m_callTime;

		private String m_callUrl;

		private String m_sqlUrl;

		private String m_cacheUrl;

		private double m_callTimePercent;

		private int m_count;

		private String m_name;

		private double m_sqlAvg;

		private int m_sqlMax;

		private int m_sqlMin;

		private int m_sqlTime;

		private double m_sqlTimePercent;

		private String m_type;

		private String m_url;

		public double getAvg() {
			return m_avg;
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

		public String getCacheUrl() {
			return m_cacheUrl;
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

		public String getCallUrl() {
			return m_callUrl;
		}

		public int getCount() {
			return m_count;
		}

		public String getName() {
			return String.valueOf(m_name);
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

		public String getSqlUrl() {
			return m_sqlUrl;
		}

		public String getType() {
			return String.valueOf(m_type);
		}

		public String getUrl() {
			return m_url;
		}

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
			if (ratio == null) {
				return;
			}

			m_cacheMin = ratio.getMin();
			m_cacheMax = ratio.getMax();
			m_cacheUrl = ratio.getUrl();
			if (matrix.getCount() > 0) {
				m_cacheAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			if (m_cacheAvg > 0) {
				m_cacheTime = (int) ((double) ratio.getTotalTime() / 1000 / m_cacheAvg / m_count);
			}
			if (matrix.getTotalTime() > 0) {
				m_cacheTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}

		public void setCallInfo(Matrix matrix) {
			Ratio ratio = matrix.getRatios().get("Call");
			if (ratio == null) {
				return;
			}

			m_callMin = ratio.getMin();
			m_callMax = ratio.getMax();
			m_callUrl = ratio.getUrl();
			if (matrix.getCount() > 0) {
				m_callAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			if (m_callAvg > 0) {
				m_callTime = (int) ((double) ratio.getTotalTime() / 1000 / m_callAvg / m_count);
			}
			if (matrix.getTotalTime() > 0) {
				m_callTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}

		public void setSQLInfo(Matrix matrix) {
			Ratio ratio = matrix.getRatios().get("SQL");
			if (ratio == null) {
				return;
			}

			m_sqlMin = ratio.getMin();
			m_sqlMax = ratio.getMax();
			m_sqlUrl = ratio.getUrl();
			if (matrix.getCount() > 0) {
				m_sqlAvg = (double) ratio.getTotalCount() / (double) matrix.getCount();
			}
			if (m_sqlAvg > 0) {
				m_sqlTime = (int) ((double) ratio.getTotalTime() / 1000 / m_sqlAvg / m_count);
			}
			if (matrix.getTotalTime() > 0) {
				m_sqlTimePercent = (double) ratio.getTotalTime() / (double) (matrix.getTotalTime());
			}
		}
	}

	public static class MatrixItemCompartor implements Comparator<MatrixItem> {
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
				return o2.getCallMin() - o1.getCallMin();
			}
			if (m_sort.equalsIgnoreCase("CallMaxCount")) {
				return o2.getCallMax() - o1.getCallMax();
			}
			if (m_sort.equalsIgnoreCase("CallAvgCount")) {
				return (int) (o2.getCallAvg() * 100 - o1.getCallAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("CallAvgTotalTime")) {
				return o2.getCallTime() - o1.getCallTime();
			}
			if (m_sort.equalsIgnoreCase("callTimePercent")) {
				return (int) (o2.getCallTimePercent() * 100 - o1.getCallTimePercent() * 100);
			}
			if (m_sort.equalsIgnoreCase("SqlMinCount")) {
				return o2.getSqlMin() - o1.getSqlMin();
			}
			if (m_sort.equalsIgnoreCase("SqlMaxCount")) {
				return o2.getSqlMax() - o1.getSqlMax();
			}
			if (m_sort.equalsIgnoreCase("SqlAvgCount")) {
				return (int) (o2.getSqlAvg() * 100 - o1.getSqlAvg() * 100);
			}
			if (m_sort.equalsIgnoreCase("SqlAvgTotalTime")) {
				return o2.getSqlTime() - o1.getSqlTime();
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
				return o2.getCacheTime() - o1.getCacheTime();
			}
			if (m_sort.equalsIgnoreCase("CacheTimePercent")) {
				return (int) (o2.getCacheTimePercent() * 100 - o1.getCacheTimePercent() * 100);
			}
			return 0;
		}
	}
}
