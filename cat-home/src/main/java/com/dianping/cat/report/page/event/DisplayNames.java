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
package com.dianping.cat.report.page.event;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;

public class DisplayNames {

	private List<EventNameModel> m_results = new ArrayList<EventNameModel>();

	public DisplayNames display(String sorted, String type, String ip, EventReport report) {
		Map<String, EventType> types = report.findOrCreateMachine(ip).getTypes();
		EventName all = new EventName("TOTAL");
		all.setTotalPercent(1);
		if (types != null) {
			EventType names = types.get(type);

			if (names != null) {
				for (Entry<String, EventName> entry : names.getNames().entrySet()) {
					m_results.add(new EventNameModel(entry.getKey(), entry.getValue()));
					mergeName(all, entry.getValue());
				}
			}
		}
		if (sorted == null) {
			sorted = "avg";
		}
		Collections.sort(m_results, new EventComparator(sorted));

		long total = all.getTotalCount();
		for (EventNameModel nameModel : m_results) {
			EventName eventName = nameModel.getDetail();
			eventName.setTotalPercent(eventName.getTotalCount() / (double) total);
		}
		m_results.add(0, new EventNameModel("TOTAL", all));
		return this;
	}

	public List<EventNameModel> getResults() {
		return m_results;
	}

	public void mergeName(EventName old, EventName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	public static class EventComparator implements Comparator<EventNameModel> {

		private String m_sorted;

		public EventComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(EventNameModel m1, EventNameModel m2) {
			if (m_sorted.equals("name") || m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				return (int) (m2.getDetail().getTotalCount() - m1.getDetail().getTotalCount());
			}
			if (m_sorted.equals("failure")) {
				return (int) (m2.getDetail().getFailCount() - m1.getDetail().getFailCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return (int) (m2.getDetail().getFailPercent() * 100 - m1.getDetail().getFailPercent() * 100);
			}
			return 0;
		}
	}

	public static class EventNameModel {
		private EventName m_detail;

		private String m_type;

		public EventNameModel(String str, EventName detail) {
			m_type = str;
			m_detail = detail;
		}

		public EventName getDetail() {
			return m_detail;
		}

		public String getName() {
			String id = m_detail.getId();

			try {
				return URLEncoder.encode(id, "utf-8");
			} catch (Exception e) {
				return id;
			}
		}

		public String getType() {
			return m_type;
		}
	}

}
