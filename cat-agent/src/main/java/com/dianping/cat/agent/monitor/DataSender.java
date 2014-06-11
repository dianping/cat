package com.dianping.cat.agent.monitor;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;

public class DataSender implements Task {

	@Inject
	private EnvironmentConfig m_config;

	private BlockingQueue<DataEntity> m_entities = new ArrayBlockingQueue<DataEntity>(5000);

	public boolean put(List<DataEntity> entities) {
		boolean result = true;

		for (DataEntity entity : entities) {
			boolean temp = m_entities.offer(entity);

			if (!temp) {
				result = temp;
			}
		}
		return result;
	}

	private void send(DataEntity entity) {
		List<String> servers = m_config.getServers();

		for (String server : servers) {
			try {
				String url = buildUrl(server, entity);
				InputStream in = Urls.forIO().readTimeout(3000).connectTimeout(3000).openStream(url);
				String content = Files.forIO().readFrom(in, "utf-8");

				if (sendOK(content)) {
					break;
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private boolean sendOK(String content) {
		return true;
	}

	private String buildUrl(String server, DataEntity entity) {
		return null;
	}

	@Override
	public void run() {
		while (true) {
			try {
				DataEntity entity = m_entities.poll(5, TimeUnit.MILLISECONDS);

				if (entity != null) {
					send(entity);
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public String getName() {
		return "system-data-sender";
	}

	@Override
	public void shutdown() {
	}

}
