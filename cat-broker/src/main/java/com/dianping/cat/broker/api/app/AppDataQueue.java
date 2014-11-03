package com.dianping.cat.broker.api.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppDataQueue<T> {
	private BlockingQueue<T> m_datas = new LinkedBlockingQueue<T>(100000);

	public boolean offer(T appData) {
		return m_datas.offer(appData);
	}

	public T poll() {
		T appData;

		try {
			appData = m_datas.poll(5, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			return null;
		}
		return appData;
	}
}
