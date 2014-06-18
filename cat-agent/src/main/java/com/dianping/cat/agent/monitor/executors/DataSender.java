package com.dianping.cat.agent.monitor.executors;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.EnvironmentConfig;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class DataSender implements Task, Initializable {

	@Inject
	private EnvironmentConfig m_environmentConfig;

	private static BlockingQueue<DataEntity> m_entities = new ArrayBlockingQueue<DataEntity>(5000);

	private List<DataEntity> m_dataEntities = new ArrayList<DataEntity>();

	private static final long DURATION = 20 * 1000;

	private static final int MAX_ENTITIES = 30;

	public boolean put(List<DataEntity> entities) {
		boolean result = true;

		try {
			for (DataEntity entity : entities) {
				boolean temp = m_entities.offer(entity, 5, TimeUnit.MILLISECONDS);

				if (!temp) {
					result = temp;
				}
			}
			return result;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

	private String buildBatchEntities(List<DataEntity> entities) {
		StringBuilder sb = new StringBuilder();

		for (DataEntity entity : entities) {
			sb.append(entity.getId()).append("\t").append(entity.getType()).append("\t").append(entity.getTime())
			      .append("\t").append(entity.getValue()).append("\n");
		}
		return sb.toString();
	}

	private boolean sendData(String url, String content) {
		try {
			URLConnection conn = new URL(url).openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);

			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(content);
			writer.flush();

			InputStream in = conn.getInputStream();
			String result = Files.forIO().readFrom(in, "utf-8");

			if (result.contains("{\"statusCode\":\"0\"}")) {
				return true;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

	private boolean sendBatchEntities(List<DataEntity> entities) {
		List<String> servers = m_environmentConfig.getServers();

		for (String server : servers) {
			String url = m_environmentConfig.buildSystemUrl(server);
			String entityContent = buildBatchEntities(m_dataEntities);
			String content = "&batch=" + entityContent;

			if (sendData(url, content)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Transaction t = Cat.newTransaction("Data", "Send");
			long current = System.currentTimeMillis();

			try {
				int maxSize = MAX_ENTITIES;

				try {
					while (m_entities.size() > 0 && maxSize > 0) {
						DataEntity entity = m_entities.poll(5, TimeUnit.MILLISECONDS);

						m_dataEntities.add(entity);
						maxSize--;
					}
					if (!m_dataEntities.isEmpty()) {
						boolean success = sendBatchEntities(m_dataEntities);

						if (!success) {
							Cat.logEvent("DataSender", "Failed", Event.SUCCESS, m_dataEntities.toString());
						}
					}
					maxSize = MAX_ENTITIES;
				} catch (Exception e) {
					Cat.logError(e);
				}
				long duration = System.currentTimeMillis() - current;

				try {
					if (duration < DURATION) {
						Thread.sleep(DURATION - duration);
					}
				} catch (InterruptedException e) {
					active = false;
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
			} finally {
				t.complete();
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

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(this);
	}

}
