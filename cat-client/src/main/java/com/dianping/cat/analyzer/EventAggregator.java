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
package com.dianping.cat.analyzer;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EventAggregator {

	private static EventAggregator s_instance = new EventAggregator();

	private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> m_events = new ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>>();

	public static EventAggregator getInstance() {
		return s_instance;
	}

	private EventData createEventData(String type, String name) {
		return new EventData(type, name);
	}

	public ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> getAndResetEvents() {
		ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> cloned = m_events;

		m_events = new ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>>();

		for (Map.Entry<String, ConcurrentHashMap<String, EventData>> entry : cloned.entrySet()) {
			String type = entry.getKey();

			m_events.putIfAbsent(type, new ConcurrentHashMap<String, EventData>());
		}

		return cloned;
	}

	public String getDomain(MessageTree tree) {
		return Cat.getManager().getDomain();
	}

	public void logBatchEvent(String type, String name, int total, int fail) {
		makeSureEventExist(type, name).add(total, fail);
	}

	public void logEvent(Event e) {
		makeSureEventExist(e.getType(), e.getName()).add(e);
	}

	private EventData makeSureEventExist(String type, String name) {
		ConcurrentHashMap<String, EventData> item = m_events.get(type);

		if (null == item) {
			item = new ConcurrentHashMap<String, EventData>();

			ConcurrentHashMap<String, EventData> oldValue = m_events.putIfAbsent(type, item);

			if (oldValue != null) {
				item = oldValue;
			}
		}

		EventData data = item.get(name);

		if (null == data) {
			data = createEventData(type, name);

			EventData oldValue = item.putIfAbsent(name, data);

			if (oldValue == null) {
				return data;
			} else {
				return oldValue;
			}
		}

		return data;
	}

	public void sendEventData() {
		ConcurrentHashMap<String, ConcurrentHashMap<String, EventData>> events = getAndResetEvents();
		boolean hasData = false;

		for (Map<String, EventData> entry : events.values()) {
			for (EventData data : entry.values()) {
				if (data.getCount() > 0) {
					hasData = true;
					break;
				}
			}
		}

		if (hasData) {
			Transaction t = Cat.newTransaction(CatConstants.CAT_SYSTEM, this.getClass().getSimpleName());
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

			tree.setDomain(getDomain(tree));
			tree.setDiscardPrivate(false);

			for (Map<String, EventData> entry : events.values()) {
				for (EventData data : entry.values()) {
					if (data.getCount() > 0) {
						Event tmp = Cat.newEvent(data.getType(), data.getName());
						StringBuilder sb = new StringBuilder(32);

						sb.append(CatConstants.BATCH_FLAG).append(data.getCount()).append(CatConstants.SPLIT).append(data.getError());
						tmp.addData(sb.toString());
						tmp.setSuccessStatus();
						tmp.complete();
					}
				}
			}

			t.setSuccessStatus();
			t.complete();
		}
	}

	public class EventData {

		private String m_type;

		private String m_name;

		private AtomicInteger m_count = new AtomicInteger();

		private AtomicInteger m_error = new AtomicInteger();

		public EventData(String type, String name) {
			m_type = type;
			m_name = name;
		}

		public EventData add(Event e) {
			m_count.incrementAndGet();

			if (!e.isSuccess()) {
				m_error.incrementAndGet();
			}
			return this;
		}

		public EventData add(int count, int fail) {
			m_count.addAndGet(count);
			m_error.addAndGet(fail);
			return this;
		}

		public int getCount() {
			return m_count.get();
		}

		public int getError() {
			return m_error.get();
		}

		public String getName() {
			return m_name;
		}

		public String getType() {
			return m_type;
		}
	}

}
