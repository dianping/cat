package com.dianping.cat.broker.api.app;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.broker.api.app.proto.ProtoData;

public class AppQueue {
	private int MAX_SIZE = 100000;

	private BlockingQueue<ProtoData> m_datas = new LinkedBlockingQueue<ProtoData>(MAX_SIZE);

	public boolean offer(ProtoData appData) {
		return m_datas.offer(appData);
	}

	public ProtoData poll() {
		ProtoData appData;

		try {
			appData = m_datas.poll(5, TimeUnit.MICROSECONDS);
		} catch (InterruptedException e) {
			return null;
		}
		return appData;
	}
}
