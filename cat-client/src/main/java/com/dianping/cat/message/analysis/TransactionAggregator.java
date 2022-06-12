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
package com.dianping.cat.message.analysis;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.util.Splitters;

// Component
public class TransactionAggregator implements Initializable {
	// Inject
	private ConfigureManager m_configureManager;

	private AtomicTreeParser m_atomicTreeParser = new AtomicTreeParser();

	private Map<String, List<Integer>> m_longConfigs = new LinkedHashMap<>();

	private volatile ConcurrentMap<String, ConcurrentMap<String, TransactionData>> m_transactions = new ConcurrentHashMap<>();

	private int checkAndGetLongThreshold(String type, int duration) {
		ProblemLongType longType = ProblemLongType.findByMessageType(type);

		if (longType != null) {
			switch (longType) {
			case LONG_CACHE:
				return getLongThresholdByDuration(ProblemLongType.LONG_CACHE.getName(), duration);
			case LONG_CALL:
				return getLongThresholdByDuration(ProblemLongType.LONG_CALL.getName(), duration);
			case LONG_SERVICE:
				return getLongThresholdByDuration(ProblemLongType.LONG_SERVICE.getName(), duration);
			case LONG_SQL:
				return getLongThresholdByDuration(ProblemLongType.LONG_SQL.getName(), duration);
			case LONG_URL:
				return getLongThresholdByDuration(ProblemLongType.LONG_URL.getName(), duration);
			case LONG_MQ:
				return getLongThresholdByDuration(ProblemLongType.LONG_MQ.getName(), duration);
			}
		}
		return -1;
	}

	public void refreshConfig() {
		String startTypes = m_configureManager.getProperty(ConfigureProperty.START_TRANSACTION_TYPES, "");
		String matchTypes = m_configureManager.getProperty(ConfigureProperty.MATCH_TRANSACTION_TYPES, "");

		m_atomicTreeParser.init(startTypes, matchTypes);

		for (ProblemLongType longType : ProblemLongType.values()) {
			final String name = longType.getName();
			String propertyName = name + "s";
			String values = m_configureManager.getProperty(propertyName, null);

			if (values != null) {
				List<String> valueStrs = Splitters.by(',').trim().split(values);
				List<Integer> thresholds = new LinkedList<Integer>();

				for (String valueStr : valueStrs) {
					try {
						thresholds.add(Integer.parseInt(valueStr));
					} catch (Exception e) {
						// ignore
					}
				}

				if (!thresholds.isEmpty()) {
					m_longConfigs.put(name, thresholds);
				}
			}
		}
	}

	private int getLongThresholdByDuration(String key, int duration) {
		List<Integer> values = m_longConfigs.get(key);

		if (values != null) {
			for (int i = values.size() - 1; i >= 0; i--) {
				int userThreshold = values.get(i);

				if (duration >= userThreshold) {
					return userThreshold;
				}
			}
		}

		return -1;
	}

	private TransactionData createTransactionData(String type, String name) {
		return new TransactionData(type, name);
	}

	public ConcurrentMap<String, ConcurrentMap<String, TransactionData>> getAndResetTransactions() {
		ConcurrentMap<String, ConcurrentMap<String, TransactionData>> cloned = m_transactions;

		m_transactions = new ConcurrentHashMap<>();

		for (Entry<String, ConcurrentMap<String, TransactionData>> entry : cloned.entrySet()) {
			String type = entry.getKey();

			m_transactions.putIfAbsent(type, new ConcurrentHashMap<String, TransactionData>());
		}

		return cloned;
	}

	public String getDomain(MessageTree tree) {
		return m_configureManager.getDomain();
	}

	public void logBatchTransaction(String type, String name, int count, int error, long sum) {
		makeSureTransactionExist(type, name).add(count, error, sum);
	}

	public void logTransaction(Transaction t) {
		makeSureTransactionExist(t.getType(), t.getName()).add(t);
	}

	private TransactionData makeSureTransactionExist(String type, String name) {
		ConcurrentMap<String, TransactionData> item = m_transactions.get(type);

		if (null == item) {
			item = new ConcurrentHashMap<String, TransactionData>();

			ConcurrentMap<String, TransactionData> oldValue = m_transactions.putIfAbsent(type, item);

			if (oldValue != null) {
				item = oldValue;
			}
		}

		TransactionData data = item.get(name);

		if (null == data) {
			data = createTransactionData(type, name);

			TransactionData oldValue = item.putIfAbsent(name, data);

			if (oldValue == null) {
				return data;
			} else {
				return oldValue;
			}
		}

		return data;
	}

	public void sendTransactionData() {
		ConcurrentMap<String, ConcurrentMap<String, TransactionData>> transactions = getAndResetTransactions();
		boolean hasData = false;

		for (Map<String, TransactionData> entry : transactions.values()) {
			for (TransactionData data : entry.values()) {
				if (data.getCount().get() > 0) {
					hasData = true;
					break;
				}
			}
		}

		if (hasData) {
			Transaction t = Cat.newTransaction(CatClientConstants.CAT_SYSTEM, this.getClass().getSimpleName());
			MessageTree tree = null; // Cat.getManager().getThreadLocalMessageTree();

			tree.setDomain(getDomain(tree));
			tree.setDiscard(false);

			for (Map<String, TransactionData> entry : transactions.values()) {
				for (TransactionData data : entry.values()) {
					if (data.getCount().get() > 0) {
						Transaction tmp = Cat.newTransaction(data.getType(), data.getName());
						StringBuilder sb = new StringBuilder(32);

						sb.append(CatClientConstants.BATCH_FLAG).append(data.getCount().get())
						      .append(CatClientConstants.SPLIT);
						sb.append(data.getFail().get()).append(CatClientConstants.SPLIT);
						sb.append(data.getSum().get()).append(CatClientConstants.SPLIT);
						sb.append(data.getDurationString()).append(CatClientConstants.SPLIT)
						      .append(data.getLongDurationString());

						tmp.addData(sb.toString());
						tmp.success();
						tmp.complete();
					}
				}
			}
			t.success();
			t.complete();
		}
	}

	private class TransactionData {
		private String m_type;

		private String m_name;

		private AtomicInteger m_count = new AtomicInteger();

		private AtomicInteger m_fail = new AtomicInteger();

		private AtomicLong m_sum = new AtomicLong();

		private ConcurrentHashMap<Integer, AtomicInteger> m_durations = new ConcurrentHashMap<Integer, AtomicInteger>();

		private ConcurrentHashMap<Integer, AtomicInteger> m_longDurations = new ConcurrentHashMap<Integer, AtomicInteger>();

		public TransactionData(String type, String name) {
			m_type = type;
			m_name = name;
		}

		public TransactionData add(int count, int error, long sum) {
			m_count.addAndGet(count);
			m_sum.addAndGet(sum);
			m_fail.addAndGet(error);

			if (count == 1) {
				int duration = DurationComputer.computeDuration((int) sum);
				AtomicInteger durationCount = m_durations.get(duration);

				if (durationCount == null) {
					m_durations.put(duration, new AtomicInteger(1));
				} else {
					durationCount.incrementAndGet();
				}
			}

			return this;
		}

		public TransactionData add(Transaction t) {
			m_count.incrementAndGet();
			m_sum.getAndAdd(t.getDurationInMillis());

			if (!t.isSuccess()) {
				m_fail.incrementAndGet();
			}

			int duration = DurationComputer.computeDuration((int) t.getDurationInMillis());
			AtomicInteger count = m_durations.get(duration);

			if (count == null) {
				count = new AtomicInteger(0);

				AtomicInteger oldCount = m_durations.putIfAbsent(duration, count);

				if (oldCount != null) {
					count = oldCount;
				}
			}
			count.incrementAndGet();

			int longDuration = checkAndGetLongThreshold(t.getType(), duration);

			if (longDuration > 0) {
				AtomicInteger longCount = m_longDurations.get(longDuration);

				if (longCount == null) {
					longCount = new AtomicInteger(0);

					AtomicInteger oldLongCount = m_longDurations.putIfAbsent(longDuration, longCount);

					if (oldLongCount != null) {
						longCount = oldLongCount;
					}
				}
				longCount.incrementAndGet();
			}
			return this;
		}

		public AtomicInteger getCount() {
			return m_count;
		}

		public String getDurationString() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;

			for (Entry<Integer, AtomicInteger> entry : m_durations.entrySet()) {
				Integer key = entry.getKey();
				AtomicInteger value = entry.getValue();

				if (first) {
					sb.append(key).append(',').append(value);
					first = false;
				} else {
					sb.append('|').append(key).append(',').append(value);
				}
			}

			return sb.toString();
		}

		public AtomicInteger getFail() {
			return m_fail;
		}

		public String getLongDurationString() {
			StringBuilder sb = new StringBuilder();
			boolean first = true;

			for (Entry<Integer, AtomicInteger> entry : m_longDurations.entrySet()) {
				Integer key = entry.getKey();
				AtomicInteger value = entry.getValue();

				if (first) {
					sb.append(key).append(',').append(value);
					first = false;
				} else {
					sb.append('|').append(key).append(',').append(value);
				}
			}

			return sb.toString();
		}

		public String getName() {
			return m_name;
		}

		public AtomicLong getSum() {
			return m_sum;
		}

		public String getType() {
			return m_type;
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
	}

	public boolean isAtomicMessage(MessageTree tree) {
		return m_atomicTreeParser.isAtomicMessage(tree);
	}
}
