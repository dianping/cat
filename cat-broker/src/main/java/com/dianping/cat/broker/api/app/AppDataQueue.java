package com.dianping.cat.broker.api.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppDataQueue {
	private int MAX_SIZE = 100000;

	private BlockingQueue<BaseData> m_datas = new LinkedBlockingQueue<BaseData>(MAX_SIZE);

	public boolean offer(BaseData appData) {
		return m_datas.offer(appData);
	}

	public BaseData poll() {
		BaseData appData;

		try {
			appData = m_datas.poll(5, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			return null;
		}
		return appData;
	}
}
