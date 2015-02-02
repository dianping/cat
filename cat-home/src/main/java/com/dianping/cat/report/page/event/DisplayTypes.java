package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.CatConstants;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;

public class DisplayTypes {

	public static final Set<String> s_unusedTypes = new HashSet<String>();

	static {
		s_unusedTypes.add(CatConstants.TYPE_URL);
		s_unusedTypes.add(CatConstants.TYPE_SQL_PARAM);
		s_unusedTypes.add(CatConstants.TYPE_PIGEON_REQUEST);
		s_unusedTypes.add(CatConstants.TYPE_PIGEON_RESPONSE);
		s_unusedTypes.add(CatConstants.TYPE_REMOTE_CALL);
	}

	private List<EventTypeModel> m_results = new ArrayList<EventTypeModel>();

	private boolean m_showAll = false;

	public DisplayTypes display(String sorted, String ip, boolean showAll, EventReport report) {
		if (report == null) {
			return this;
		}
		m_showAll = showAll;
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
			return this;
		}
		Map<String, EventType> types = machine.getTypes();
		if (types != null) {
			for (Entry<String, EventType> entry : types.entrySet()) {
				if (shouldShow(entry.getKey())) {
					m_results.add(new EventTypeModel(entry.getKey(), entry.getValue()));
				}
			}
		}
		if (!StringUtils.isEmpty(sorted)) {
			Collections.sort(m_results, new EventComparator(sorted));
		}
		return this;
	}

	public List<EventTypeModel> getResults() {
		return m_results;
	}

	public DisplayTypes setShowAll(boolean showAll) {
		m_showAll = showAll;
		return this;
	}

	private boolean shouldShow(String type) {
		if (m_showAll) {
			return true;
		}
		if (s_unusedTypes.contains(type)) {
			return false;
		}
		return true;
	}

	public static class EventComparator implements Comparator<EventTypeModel> {

		private String m_sorted;

		public EventComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(EventTypeModel m1, EventTypeModel m2) {
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

	public static class EventTypeModel {
		private EventType m_detail;

		private String m_type;

		public EventTypeModel(String str, EventType detail) {
			m_type = str;
			m_detail = detail;
		}

		public EventType getDetail() {
			return m_detail;
		}

		public String getType() {
			return m_type;
		}
	}
}
