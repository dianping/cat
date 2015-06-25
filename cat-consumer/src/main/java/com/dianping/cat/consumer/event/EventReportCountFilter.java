package com.dianping.cat.consumer.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventReportCountFilter extends BaseVisitor {

	private int m_maxItems = 400;

	private void mergeName(EventName old, EventName other) {
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

	@Override
	public void visitType(EventType type) {
		Map<String, EventName> eventNames = type.getNames();
		Set<String> names = eventNames.keySet();
		Set<String> invalidates = new HashSet<String>();

		for (String temp : names) {
			int length = temp.length();

			for (int i = 0; i < length; i++) {
				// invalidate char
				if (temp.charAt(i) > 126 || temp.charAt(i) < 32) {
					invalidates.add(temp);
					break;
				}
			}
		}

		for (String name : invalidates) {
			eventNames.remove(name);
		}

		int size = eventNames.size();

		if (size > m_maxItems) {
			List<EventName> all = new ArrayList<EventName>(eventNames.values());

			Collections.sort(all, new EventNameCompator());
			type.getNames().clear();

			for (int i = 0; i < m_maxItems; i++) {
				type.addName(all.get(i));
			}

			EventName other = type.findOrCreateName("OTHERS");

			for (int i = m_maxItems; i < size; i++) {
				mergeName(other, all.get(i));
			}
		}
		super.visitType(type);
	}

	private static class EventNameCompator implements Comparator<EventName> {
		@Override
		public int compare(EventName o1, EventName o2) {
			return (int) (o2.getTotalCount() - o1.getTotalCount());
		}
	}
}