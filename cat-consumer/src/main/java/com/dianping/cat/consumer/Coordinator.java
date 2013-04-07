package com.dianping.cat.consumer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ServerConfigManager;

public class Coordinator {

	@Inject
	private ServerConfigManager m_manager;

	private ReadWriteLock m_lock = new ReentrantReadWriteLock();

	private Lock m_parallel = m_lock.readLock();

	private Lock m_serial = m_lock.writeLock();

	public void acquireWrite() throws InterruptedException {
		boolean isSerial = m_manager.isSerialWrite();

		if (isSerial) {
			m_serial.lock();
		} else {
			m_parallel.lock();
		}
	}

	public void releaseWrite() {
		boolean isSerial = m_manager.isSerialWrite();

		if (isSerial) {
			m_serial.unlock();
		} else {
			m_parallel.unlock();
		}
	}
}
