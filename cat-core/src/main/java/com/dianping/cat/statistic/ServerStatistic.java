package com.dianping.cat.statistic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ServerStatistic {
	private Map<Long, Statistic> m_statistics = new ConcurrentHashMap<Long, Statistic>(100);

	public Statistic findOrCreate(Long time) {
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

		private double m_messageSize;

		private long m_messageDump;

		private long m_messageDumpLoss;

		private Map<String, AtomicLong> m_messageTotals = new ConcurrentHashMap<String, AtomicLong>(256);

		private Map<String, AtomicLong> m_messageTotalLosses = new ConcurrentHashMap<String, AtomicLong>(256);

		private Map<String, Double> m_messageSizes = new ConcurrentHashMap<String, Double>(256);

		private double m_processDelaySum;

		private int m_processDelayCount;

		private double m_avgTps;

		private double m_maxTps;

		private long m_blockTotal;

		private long m_blockLoss;

		private long m_blockTime;

		private long m_pigeonTimeError;

		private long m_networkTimeError;

		public void addBlockTotal(long block) {
			m_blockTotal += block;
		}

		public void addBlockLoss(long blockLoss) {
			m_blockLoss += blockLoss;
		}

		public void addPigeonTimeError(long pigeonTimeError) {
			m_pigeonTimeError += pigeonTimeError;
		}

		public void addNetworkTimeError(long networkTimeError) {
			m_networkTimeError += networkTimeError;
		}

		public void addMessageDump(long messageDump) {
			m_messageDump += messageDump;
		}

		public void addMessageDumpLoss(long messageDumpLoss) {
			m_messageDumpLoss += messageDumpLoss;
		}

		public void addMessageTotal(String domain, long messageTotal) {
			AtomicLong value = m_messageTotals.get(domain);
			if (value != null) {
				value.set(value.get() + messageTotal);
			} else {
				m_messageTotals.put(domain, new AtomicLong(messageTotal));
			}
		}

		public void addMessageTotalLoss(String domain, long messageTotalLoss) {
			AtomicLong value = m_messageTotalLosses.get(domain);
			if (value != null) {
				value.set(value.get() + messageTotalLoss);
			} else {
				m_messageTotalLosses.put(domain, new AtomicLong(messageTotalLoss));
			}
		}

		public void addMessageSize(String domain, double messageSize) {
			Double value = m_messageSizes.get(domain);
			if (value != null) {
				m_messageSizes.put(domain, value + messageSize);
			} else {
				m_messageSizes.put(domain, messageSize);
			}
		}

		public void addMessageTotal(long messageTotal) {
			m_messageTotal += messageTotal;
		}

		public void addMessageTotalLoss(long messageTotalLoss) {
			m_messageTotalLoss += messageTotalLoss;
		}

		public void addMessageSize(double messageSize) {
			m_messageSize += messageSize;
		}

		public void addBlockTime(long blockTime) {
			m_blockTime += blockTime;
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

		public double getAvgTps() {
			return m_avgTps;
		}

		public void setAvgTps(double avgTps) {
			m_avgTps = avgTps;
		}

		public double getMaxTps() {
			return m_maxTps;
		}

		public void setMaxTps(double maxTps) {
			m_maxTps = maxTps;
		}

		public long getMessageDump() {
			return m_messageDump;
		}

		public long getMessageDumpLoss() {
			return m_messageDumpLoss;
		}

		public Map<String, Double> getMessageSizes() {
			return m_messageSizes;
		}

		public Map<String, AtomicLong> getMessageTotals() {
			return m_messageTotals;
		}

		public Map<String, AtomicLong> getMessageTotalLosses() {
			return m_messageTotalLosses;
		}

		public int getProcessDelayCount() {
			return m_processDelayCount;
		}

		public double getProcessDelaySum() {
			return m_processDelaySum;
		}

		public void setProcessDelaySum(double processDelaySum) {
			m_processDelaySum = processDelaySum;
		}

		public void setProcessDelayCount(int processDelayCount) {
			m_processDelayCount = processDelayCount;
		}

		public long getBlockTotal() {
			return m_blockTotal;
		}

		public long getBlockLoss() {
			return m_blockLoss;
		}

		public long getPigeonTimeError() {
			return m_pigeonTimeError;
		}

		public long getNetworkTimeError() {
			return m_networkTimeError;
		}

		public long getBlockTime() {
			return m_blockTime;
		}

		public long getMessageTotal() {
			return m_messageTotal;
		}

		public long getMessageTotalLoss() {
			return m_messageTotalLoss;
		}

		public double getMessageSize() {
			return m_messageSize;
		}
	}

}
