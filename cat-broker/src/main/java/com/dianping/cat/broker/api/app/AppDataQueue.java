package com.dianping.cat.broker.api.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppDataQueue {
	private int MAX_SIZE = 100000;

	private BlockingQueue<AppData> m_datas = new LinkedBlockingQueue<AppData>(MAX_SIZE);

	public boolean offer(AppData appData) {
		return m_datas.offer(appData);
	}

	public AppData poll() {
		AppData appData;

		try {
			appData = m_datas.poll(5, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			return null;
		}
		return appData;
	}
}
