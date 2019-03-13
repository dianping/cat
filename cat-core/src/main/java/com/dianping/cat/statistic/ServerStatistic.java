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
package com.dianping.cat.statistic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerStatistic {
	private Map<Long, Statistic> m_statistics = new ConcurrentHashMap<Long, Statistic>(100);

	public synchronized Statistic findOrCreate(Long time) {
		Statistic state = m_statistics.get(time);

		if (state == null) {
			state = new Statistic();
			m_statistics.put(time, state);
		}
		return state;
	}

	public void remove(long time) {
		m_statistics.remove(time);
	}

	public static class Statistic {

		private long m_messageTotal;

		private long m_messageTotalLoss;

		private long m_messageSize;

		private long m_messageDump;

		private long m_messageDumpLoss;

		private ConcurrentMap<String, AtomicLong> m_messageTotals = new ConcurrentHashMap<String, AtomicLong>(256);

		private ConcurrentMap<String, AtomicLong> m_messageTotalLosses = new ConcurrentHashMap<String, AtomicLong>(256);

		private ConcurrentMap<String, AtomicLong> m_messageSizes = new ConcurrentHashMap<String, AtomicLong>(256);

		private double m_processDelaySum;

		private int m_processDelayCount;

		private long m_blockTotal;

		private long m_blockLoss;

		private long m_blockTime;

		private long m_pigeonTimeError;

		private long m_networkTimeError;

		public void addBlockLoss(long blockLoss) {
			m_blockLoss += blockLoss;
		}

		public void addBlockTime(long blockTime) {
			m_blockTime += blockTime;
		}

		public void addBlockTotal(long block) {
			m_blockTotal += block;
		}

		public void addMessageDump(long messageDump) {
			m_messageDump += messageDump;
		}

		public void addMessageDumpLoss(long messageDumpLoss) {
			m_messageDumpLoss += messageDumpLoss;
		}

		public void addMessageSize(String domain, int size) {
			m_messageSize += size;

			AtomicLong value = m_messageSizes.get(domain);

			if (value != null) {
				value.addAndGet(size);
			} else {
				m_messageSizes.put(domain, new AtomicLong(size));
			}
		}

		public void addMessageTotal(long messageTotal) {
			m_messageTotal += messageTotal;
		}

		public void addMessageTotal(String domain, long messageTotal) {
			AtomicLong value = m_messageTotals.get(domain);

			if (value != null) {
				value.addAndGet(messageTotal);
			} else {
				m_messageTotals.put(domain, new AtomicLong(messageTotal));
			}
		}

		public void addMessageTotalLoss(long messageTotalLoss) {
			m_messageTotalLoss += messageTotalLoss;
		}

		public void addMessageTotalLoss(String domain, long messageTotalLoss) {
			m_messageTotalLoss += messageTotalLoss;

			AtomicLong value = m_messageTotalLosses.get(domain);
			if (value != null) {
				value.addAndGet(messageTotalLoss);
			} else {
				m_messageTotalLosses.put(domain, new AtomicLong(messageTotalLoss));
			}
		}

		public void addNetworkTimeError(long networkTimeError) {
			m_networkTimeError += networkTimeError;
		}

		public void addPigeonTimeError(long pigeonTimeError) {
			m_pigeonTimeError += pigeonTimeError;
		}

		public void addProcessDelay(double processDelay) {
			m_processDelaySum += processDelay;
			m_processDelayCount++;
		}

		public double getAvgProcessDelay() {
			if (m_processDelayCount > 0) {
				return m_processDelaySum / m_processDelayCount;
			}
			return 0;
		}

		public long getBlockLoss() {
			return m_blockLoss;
		}

		public long getBlockTime() {
			return m_blockTime;
		}

		public long getBlockTotal() {
			return m_blockTotal;
		}

		public long getMessageDump() {
			return m_messageDump;
		}

		public long getMessageDumpLoss() {
			return m_messageDumpLoss;
		}

		public long getMessageSize() {
			return m_messageSize;
		}

		public Map<String, AtomicLong> getMessageSizes() {
			return m_messageSizes;
		}

		public long getMessageTotal() {
			return m_messageTotal;
		}

		public long getMessageTotalLoss() {
			return m_messageTotalLoss;
		}

		public Map<String, AtomicLong> getMessageTotalLosses() {
			return m_messageTotalLosses;
		}

		public Map<String, AtomicLong> getMessageTotals() {
			return m_messageTotals;
		}

		public long getNetworkTimeError() {
			return m_networkTimeError;
		}

		public long getPigeonTimeError() {
			return m_pigeonTimeError;
		}

		public int getProcessDelayCount() {
			return m_processDelayCount;
		}

		public double getProcessDelaySum() {
			return m_processDelaySum;
		}
	}

}
