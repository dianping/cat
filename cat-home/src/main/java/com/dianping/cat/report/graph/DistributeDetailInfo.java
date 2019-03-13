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

	public void setItems(List<DistributeDetail> items) {
		m_items = items;
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

	public static class DistributeDetail {
		private int m_id;

		private String m_title;

		private double m_requestSum;

		private double m_ratio;

		private double m_delayAvg;

		public int getId() {
			return m_id;
		}

		public DistributeDetail setId(int id) {
			m_id = id;
			return this;
		}

		public double getRequestSum() {
			return m_requestSum;
		}

		public DistributeDetail setRequestSum(double requestSum) {
			m_requestSum = requestSum;
			return this;
		}

		public double getRatio() {
			return m_ratio;
		}

		public DistributeDetail setRatio(double ratio) {
			m_ratio = ratio;
			return this;
		}

		public String getTitle() {
			return m_title;
		}

		public DistributeDetail setTitle(String title) {
			m_title = title;
			return this;
		}

		public double getDelayAvg() {
			return m_delayAvg;
		}

		public void setDelayAvg(double delayAvg) {
			m_delayAvg = delayAvg;
		}
	}

}
