package com.dianping.cat.network;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {
	private ThreadGroup m_threadGroup;

	private String m_name;

	private AtomicInteger m_index = new AtomicInteger();

	public DaemonThreadFactory(String name) {
		m_threadGroup = new ThreadGroup(name);
		m_name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		int nextIndex = m_index.getAndIncrement(); // always increase by one
		String threadName = m_name + "-" + nextIndex;
		Thread thread = new Thread(m_threadGroup, r, threadName);

		thread.setDaemon(true);
		return thread;
	}
}