package com.dianping.cat.consumer.impl2;

import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageQueue;

public class PeriodicTask implements Runnable {
	private long m_startTime;
	private long m_duration;
	private MessageAnalyzer m_analyzer;
	private MessageQueue m_queue;

	public PeriodicTask(long startTime, long duration,
			MessageAnalyzer analyzer, MessageQueue queue) {
	}

	public void run() {
		m_analyzer.analyze(m_queue);
	}

	public long getDuration() {
		return m_duration;
	}

	public void setDuration(long duration) {
		m_duration = duration;
	}

	public MessageQueue getQueue() {
		return m_queue;
	}

	public void setStartTime(long startTime) {
		m_startTime = startTime;
	}

}