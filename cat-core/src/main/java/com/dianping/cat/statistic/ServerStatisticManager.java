package com.dianping.cat.statistic;

import com.dianping.cat.statistic.ServerStatistic.Statistic;

public class ServerStatisticManager {

	public ServerStatistic m_serverState = new ServerStatistic();

	private volatile Statistic m_currentStatistic = null;

	private volatile long m_currentMunite = -1;

	public void addBlockLoss(long total) {
		getCurrentStatistic().addBlockLoss(total);
	}

	public void addBlockTime(long total) {
		getCurrentStatistic().addBlockTime(total);
	}

	public void addBlockTotal(long total) {
		getCurrentStatistic().addBlockTotal(total);
	}

	public void addMessageDump(long total) {
		getCurrentStatistic().addMessageDump(total);
	}

	public void addMessageDumpLoss(long total) {
		getCurrentStatistic().addMessageDumpLoss(total);
	}

	public void addMessageSize(String domain, int size) {
		getCurrentStatistic().addMessageSize(domain, size);
	}

	public void addMessageTotal(long total) {
		getCurrentStatistic().addMessageTotal(total);
	}

	public void addMessageTotal(String domain, long total) {
		getCurrentStatistic().addMessageTotal(domain, total);
	}

	public void addMessageTotalLoss(long total) {
		getCurrentStatistic().addMessageTotalLoss(total);
	}

	public void addMessageTotalLoss(String domain, long total) {
		getCurrentStatistic().addMessageTotalLoss(domain, total);
	}

	public void addNetworkTimeError(long total) {
		getCurrentStatistic().addNetworkTimeError(total);
	}

	public void addPigeonTimeError(long total) {
		getCurrentStatistic().addPigeonTimeError(total);
	}

	public void addProcessDelay(double delay) {
		getCurrentStatistic().addProcessDelay(delay);
	}

	public Statistic findOrCreateState(long time) {
		return m_serverState.findOrCreate(time);
	}

	private Statistic getCurrentStatistic() {
		long time = System.currentTimeMillis();

		time = time - time % (60 * 1000);

		if (time != m_currentMunite) {
			synchronized (this) {
				if (time != m_currentMunite) {
					m_currentStatistic = m_serverState.findOrCreate(time);
					m_currentMunite = time;
				}
			}
		}
		return m_currentStatistic;
	}

	public void removeState(long time) {
		m_serverState.remove(time);
	}
}
