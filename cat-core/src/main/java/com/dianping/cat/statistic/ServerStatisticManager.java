package com.dianping.cat.statistic;

import com.dianping.cat.statistic.ServerStatistic.Statistic;

public class ServerStatisticManager {

	public ServerStatistic m_serverState = new ServerStatistic();

	public void addBlockTotal(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addBlockTotal(total);
	}

	public void addBlockLoss(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addBlockLoss(total);
	}

	public void addBlockTime(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addBlockTime(total);
	}

	public void addPigeonTimeError(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addPigeonTimeError(total);
	}

	public void addMessageDump(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageDump(total);
	}

	public void addNetworkTimeError(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addNetworkTimeError(total);
	}

	public void addMessageDumpLoss(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageDumpLoss(total);
	}

	public void addMessageSize(String domain, double size) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageSize(domain, size);
	}

	public void addMessageTotal(String domain, long total) {
		Long time = getCurrentMinute();
		m_serverState.findOrCreate(time).addMessageTotal(domain, total);
	}

	public void addMessageTotalLoss(String domain, long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageTotalLoss(domain, total);
	}

	public void addProcessDelay(double delay) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addProcessDelay(delay);
	}

	public Statistic findState(long time) {
		return m_serverState.findOrCreate(time);
	}

	public Long getCurrentMinute() {
		long time = System.currentTimeMillis();
		return time - time % (60 * 1000);
	}

	public void removeState(long time) {
		m_serverState.remove(time);
	}
}
