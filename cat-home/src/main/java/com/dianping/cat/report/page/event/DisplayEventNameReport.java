package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.site.lookup.util.StringUtils;

public class DisplayEventNameReport {

	private List<EventNameModel> m_results = new ArrayList<EventNameModel>();

	public List<EventNameModel> getResults() {
		return m_results;
	}

	public DisplayEventNameReport display(String sorted, String type, String ip, EventReport report) {
		Map<String, EventType> types = report.getMachines().get(ip).getTypes();
		if (types != null) {
			EventType names = types.get(type);
				for (Entry<String, EventName> entry : names.getNames().entrySet()) {
					m_results.add(new EventNameModel(entry.getKey(), entry.getValue()));
			}
		}
		if (!StringUtils.isEmpty(sorted)) {
			Collections.sort(m_results, new EventComparator(sorted));
		}
		return this;
	}

	public static class EventNameModel {
		private String m_type;

		private EventName m_detail;

		public EventNameModel(String str, EventName detail) {
			m_type = str;
			m_detail = detail;
		}

		public String getType() {
			return m_type;
		}

		public EventName getDetail() {
			return m_detail;
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
}
