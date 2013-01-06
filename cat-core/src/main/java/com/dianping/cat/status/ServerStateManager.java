package com.dianping.cat.status;

import com.dianping.cat.status.ServerState.State;

public class ServerStateManager {

	public ServerState m_serverState = new ServerState();

	public void addBlockTotal(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addBlockTotal(total);
	}

	public void addBlockLoss(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addBlockLoss(total);
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

	public void addMessageSize(double size) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageSize(size);

	}

	public void addMessageTotal(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageTotal(total);
	}

	public void addMessageTotalLoss(long total) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addMessageTotalLoss(total);
	}

	public void addProcessDelay(double delay) {
		Long time = getCurrentMinute();

		m_serverState.findOrCreate(time).addProcessDelay(delay);
	}

	public State findState(long time) {
		return m_serverState.findOrCreate(time);
	}

	public Long getCurrentMinute() {
		long time = System.currentTimeMillis();
		return time - time % (60 * 1000);
	}

	public void RemoveState(long time) {
		m_serverState.remove(time);
	}
}
