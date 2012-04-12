package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.site.lookup.util.StringUtils;

public class DisplayEventReport {

	private List<EventModel> m_results = new ArrayList<EventModel>();

	public List<EventModel> getResults() {
		return m_results;
	}

	public DisplayEventReport display(String sorted, EventReport report) {
		if(report==null){
			return this;
		}
		
		Map<String, EventType> types = report.getTypes();
		if (types != null) {
			for (Entry<String, EventType> entry : types.entrySet()) {
				m_results.add(new EventModel(entry.getKey(), entry.getValue()));
			}
		}
		if (!StringUtils.isEmpty(sorted)) {
			Collections.sort(m_results, new EventComparator(sorted));
		}
		return this;
	}

	public static class EventModel {
		private String m_type;

		private EventType m_detail;

		public EventModel(String str, EventType detail) {
			m_type = str;
			m_detail = detail;
		}

		public String getType() {
			return m_type;
		}

		public EventType getDetail() {
			return m_detail;
		}
	}

	public static class EventComparator implements Comparator<EventModel> {

		private String m_sorted;

		public EventComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(EventModel m1, EventModel m2) {
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
