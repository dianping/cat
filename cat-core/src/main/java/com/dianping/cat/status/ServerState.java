package com.dianping.cat.status;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerState {
	private Map<Long, State> m_states = new LinkedHashMap<Long, State>();

	public State findOrCreate(Long time) {
		State state = m_states.get(time);

		if (state == null) {
			state = new State();
			m_states.put(time, state);
		}
		return state;
	}

	public void remove(long time) {
		m_states.remove(time);
	}

	public static class State {

		private long m_messageTotal;

		private long m_messageDump;

		private long m_messageTotalLoss;

		private long m_messageDumpLoss;

		private double m_messageSize;

		private double m_processDelaySum;

		private int m_processDelayCount;

		public void addMessageDump(long messageDump) {
			m_messageDump += messageDump;
		}

		public void addMessageDumpLoss(long messageDumpLoss) {
			m_messageDumpLoss += messageDumpLoss;
		}

		public void addMessageSize(double messageSize) {
			m_messageSize += messageSize;
		}

		public void addMessageTotal(long messageTotal) {
			m_messageTotal += messageTotal;
		}

		public void addMessageTotalLoss(long messageTotalLoss) {
			m_messageTotalLoss += messageTotalLoss;
		}

		public void addProcessDelay(double processDelay) {
			m_processDelaySum += processDelay;
			m_processDelayCount++;
		}

		public long getMessageDump() {
			return m_messageDump;
		}

		public long getMessageDumpLoss() {
			return m_messageDumpLoss;
		}

		public double getMessageSize() {
			return m_messageSize;
		}

		public long getMessageTotal() {
			return m_messageTotal;
		}

		public long getMessageTotalLoss() {
			return m_messageTotalLoss;
		}

		public int getProcessDelayCount() {
			return m_processDelayCount;
		}

		public double getProcessDelaySum() {
			return m_processDelaySum;
		}
	}

}
