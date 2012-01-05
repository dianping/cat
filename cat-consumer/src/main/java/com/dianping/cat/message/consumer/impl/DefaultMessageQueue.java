package com.dianping.cat.message.consumer.impl;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageQueue implements MessageQueue {
	private long m_duration;
	private long m_start;
	private long m_end;
	private Queue<MessageTree> queue = new LinkedBlockingQueue<MessageTree>();

	public DefaultMessageQueue() {
		m_start = System.currentTimeMillis();
		m_end = m_start + 60 * 60 * 1000;
		m_duration = 5 * 60 * 1000;
	}

	public DefaultMessageQueue(int minutes, long start) {
		m_start = start;
		m_end = start + 60 * 60 * 1000;
		m_duration = minutes * 60 * 1000;
	}

	@Override
	public boolean isActive() {
		if (queue.size() > 0) {
			return true;
		}
		return !isExpired();
	}

	public boolean isExpired() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - m_end > m_duration) {
			return true;
		}
		return false;
	}

	@Override
	public MessageTree poll() {
		return queue.poll();
	}

	@Override
	public void offer(MessageTree tree) {
		queue.add(tree);
	}

	public boolean inRange(MessageTree tree) {
		long time = tree.getMessage().getTimestamp();
		if (time < m_end && time >= m_start) {
			return true;
		}
		return false;
	}

	public long getStart() {
		return m_start;
	}

	@Override
	public int size() {
		return 0;
	}

}
