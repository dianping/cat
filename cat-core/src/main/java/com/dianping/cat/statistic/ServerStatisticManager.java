package com.dianping.cat.statistic;

import com.dianping.cat.statistic.ServerStatistic.Statistic;

public class ServerStatisticManager {

	public ServerStatistic m_serverState = new ServerStatistic();

	private Statistic m_currentStatistic = null;

	private long m_currentMunite = -1;

	public void addBlockTotal(long total) {
		getCurrentStatistic().addBlockTotal(total);
	}

	public void addBlockLoss(long total) {
		getCurrentStatistic().addBlockLoss(total);
	}

	public void addBlockTime(long total) {
		getCurrentStatistic().addBlockTime(total);
	}

	public void addPigeonTimeError(long total) {
		getCurrentStatistic().addPigeonTimeError(total);
	}

	public void addMessageDump(long total) {
		getCurrentStatistic().addMessageDump(total);
	}

	public void addNetworkTimeError(long total) {
		getCurrentStatistic().addNetworkTimeError(total);
	}

	public void addMessageDumpLoss(long total) {
		getCurrentStatistic().addMessageDumpLoss(total);
	}

	public void addMessageSize(String domain, double size) {
		getCurrentStatistic().addMessageSize(domain, size);
	}

	public void addMessageSize(double size) {
		getCurrentStatistic().addMessageSize(size);
	}

	public void addMessageTotal(String domain, long total) {
		getCurrentStatistic().addMessageTotal(domain, total);
	}

	public void addMessageTotal(long total) {
		getCurrentStatistic().addMessageTotal(total);
	}

	public void addMessageTotalLoss(String domain, long total) {
		getCurrentStatistic().addMessageTotalLoss(domain, total);
	}

	public void addMessageTotalLoss(long total) {
		getCurrentStatistic().addMessageTotalLoss(total);
	}

	public void addProcessDelay(double delay) {
		getCurrentStatistic().addProcessDelay(delay);
	}

	public Statistic findState(long time) {
		return m_serverState.findOrCreate(time);
	}

	private Statistic getCurrentStatistic() {
		long time = System.currentTimeMillis();
		
		time = time - time % (60 * 1000);
		
		synchronized(this){
			if (time != m_currentMunite) {
				m_currentMunite = time;
				m_currentStatistic = m_serverState.findOrCreate(time);
			}
		}
		
		return m_currentStatistic;
	}

	public void removeState(long time) {
		m_serverState.remove(time);
	}
}
