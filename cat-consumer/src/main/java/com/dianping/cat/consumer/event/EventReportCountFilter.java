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
package com.dianping.cat.consumer.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.consumer.util.InvidStringBuilder;

public class EventReportCountFilter extends BaseVisitor {

	private int m_maxTypeLimit;

	private int m_maxNameLimit;

	private String m_domain;

	private int m_lengthLimit;

	public EventReportCountFilter(int typeLimit, int nameLimit, int lengthLimimt) {
		m_maxTypeLimit = typeLimit;
		m_maxNameLimit = nameLimit;
		m_lengthLimit = lengthLimimt;
	}

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

	private void mergeType(EventType old, EventType other) {
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
	public void visitEventReport(EventReport eventReport) {
		m_domain = eventReport.getDomain();
		super.visitEventReport(eventReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		final Map<String, EventType> types = machine.getTypes();
		int size = types.size();

		if (size > m_maxTypeLimit) {
			Cat.logEvent("TooManyEventType", m_domain);
			List<EventType> all = new ArrayList<EventType>(types.values());
			Collections.sort(all, new EventTypeCompator());

			machine.getTypes().clear();

			for (int i = 0; i < m_maxTypeLimit; i++) {
				machine.addType(all.get(i));
			}

			EventType other = machine.findOrCreateType(CatConstants.OTHERS);

			for (int i = m_maxTypeLimit; i < size; i++) {
				mergeType(other, all.get(i));
			}
		}

		super.visitMachine(machine);
	}

	@Override
	public void visitType(EventType type) {
		Map<String, EventName> eventNames = type.getNames();
		Set<String> names = eventNames.keySet();
		Set<String> invalidates = new HashSet<String>();

		for (String temp : names) {
			int length = temp.length();

			if (length > m_lengthLimit) {
				invalidates.add(temp);
			} else {
				for (int i = 0; i < length; i++) {
					// invalidate char
					if (temp.charAt(i) > 126 || temp.charAt(i) < 32) {
						invalidates.add(temp);
						break;
					}
				}
			}
		}

		for (String name : invalidates) {
			EventName eventName = eventNames.remove(name);

			if (eventName != null) {
				eventName.setId(InvidStringBuilder.getValidString(name));
				eventNames.put(eventName.getId(), eventName);
			}
		}
		int size = eventNames.size();

		if (size > m_maxNameLimit) {
			Cat.logEvent("TooManyEventItem", m_domain + ":" + type.getId());
			List<EventName> all = new ArrayList<EventName>(eventNames.values());

			Collections.sort(all, new EventNameCompator());
			type.getNames().clear();

			for (int i = 0; i < m_maxNameLimit; i++) {
				type.addName(all.get(i));
			}

			EventName other = type.findOrCreateName(CatConstants.OTHERS);

			for (int i = m_maxNameLimit; i < size; i++) {
				mergeName(other, all.get(i));
			}
		}
		super.visitType(type);
	}

	private static class EventNameCompator implements Comparator<EventName> {
		@Override
		public int compare(EventName o1, EventName o2) {
			if (o2.getTotalCount() > o1.getTotalCount()) {
				return 1;
			} else if (o2.getTotalCount() < o1.getTotalCount()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private static class EventTypeCompator implements Comparator<EventType> {
		@Override
		public int compare(EventType o1, EventType o2) {
			if (o2.getTotalCount() > o1.getTotalCount()) {
				return 1;
			} else if (o2.getTotalCount() < o1.getTotalCount()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}