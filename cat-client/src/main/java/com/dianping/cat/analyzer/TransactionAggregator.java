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
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.ProblemLongType;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionAggregator {

	private static TransactionAggregator s_instance = new TransactionAggregator();

	private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> m_transactions = new ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>>();

	public static TransactionAggregator getInstance() {
		return s_instance;
	}

	private TransactionData createTransactionData(String type, String name) {
		return new TransactionData(type, name);
	}

	public ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> getAndResetTransactions() {
		ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> cloned = m_transactions;

		m_transactions = new ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>>();

		for (Entry<String, ConcurrentHashMap<String, TransactionData>> entry : cloned.entrySet()) {
			String type = entry.getKey();

			m_transactions.putIfAbsent(type, new ConcurrentHashMap<String, TransactionData>());
		}

		return cloned;
	}

	public String getDomain(MessageTree tree) {
		return Cat.getManager().getDomain();
	}

	public void logBatchTransaction(String type, String name, int count, int error, long sum) {
		makeSureTransactionExist(type, name).add(count, error, sum);
	}

	public void logTransaction(Transaction t) {
		makeSureTransactionExist(t.getType(), t.getName()).add(t);
	}

	private TransactionData makeSureTransactionExist(String type, String name) {
		ConcurrentHashMap<String, TransactionData> item = m_transactions.get(type);

		if (null == item) {
			item = new ConcurrentHashMap<String, TransactionData>();

			ConcurrentHashMap<String, TransactionData> oldValue = m_transactions.putIfAbsent(type, item);

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
		ConcurrentHashMap<String, ConcurrentHashMap<String, TransactionData>> transactions = getAndResetTransactions();
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
			Transaction t = Cat.newTransaction(CatConstants.CAT_SYSTEM, this.getClass().getSimpleName());
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

			tree.setDomain(getDomain(tree));
			tree.setDiscardPrivate(false);

			for (Map<String, TransactionData> entry : transactions.values()) {
				for (TransactionData data : entry.values()) {
					if (data.getCount().get() > 0) {
						Transaction tmp = Cat.newTransaction(data.getType(), data.getName());
						StringBuilder sb = new StringBuilder(32);

						sb.append(CatConstants.BATCH_FLAG).append(data.getCount().get()).append(CatConstants.SPLIT);
						sb.append(data.getFail().get()).append(CatConstants.SPLIT);
						sb.append(data.getSum().get()).append(CatConstants.SPLIT);
						sb.append(data.getDurationString()).append(CatConstants.SPLIT).append(data.getLongDurationString());

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

	private int checkAndGetLongThreshold(String type, int duration) {
		ClientConfigManager config = Cat.getManager().getConfigManager();
		ProblemLongType longType = ProblemLongType.findByMessageType(type);

		if (longType != null) {
			switch (longType) {
			case LONG_CACHE:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_CACHE.getName(), duration);
			case LONG_CALL:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_CALL.getName(), duration);
			case LONG_SERVICE:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_SERVICE.getName(), duration);
			case LONG_SQL:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_SQL.getName(), duration);
			case LONG_URL:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_URL.getName(), duration);
			case LONG_MQ:
				return config.getLongThresholdByDuration(ProblemLongType.LONG_MQ.getName(), duration);
			}
		}
		return -1;
	}

	public class TransactionData {

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

		public Map<Integer, AtomicInteger> getDurations() {
			return m_durations;
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

		public Map<Integer, AtomicInteger> getLongDurations() {
			return m_longDurations;
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

		public AtomicInteger getFail() {
			return m_fail;
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

}
