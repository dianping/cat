package com.dianping.cat.broker.api.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppDataQueue {
	private BlockingQueue<AppData> m_datas = new LinkedBlockingQueue<AppData>(100000);

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
