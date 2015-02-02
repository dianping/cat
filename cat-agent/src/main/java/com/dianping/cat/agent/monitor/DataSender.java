package com.dianping.cat.agent.monitor;

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
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class DataSender implements Task, Initializable {

	@Inject
	private CatServers m_catServers;

	private BlockingQueue<DataEntity> m_entities = new ArrayBlockingQueue<DataEntity>(5000);

	private static final int MAX_ENTITIES = 20;

	private String buildBatchEntities(List<DataEntity> entities) {
		StringBuilder sb = new StringBuilder();

		for (DataEntity entity : entities) {
			sb.append(entity.buildBatchContent());
		}
		return sb.toString();
	}

	@Override
	public String getName() {
		return "data-sender";
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(this);
	}

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

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			try {
				int maxSize = MAX_ENTITIES;
				List<DataEntity> dataEntities = new ArrayList<DataEntity>();

				while (m_entities.size() > 0 && maxSize > 0) {
					DataEntity entity = m_entities.poll(5, TimeUnit.MILLISECONDS);

					dataEntities.add(entity);
					maxSize--;
				}

				if (!dataEntities.isEmpty()) {
					Transaction t = Cat.newTransaction("Sender", "Send");
					boolean success = false;

					try {
						success = sendBatchEntities(dataEntities);
					} catch (Exception e) {
						Cat.logError(e);
					} finally {
						t.setStatus(Transaction.SUCCESS);
						t.complete();
					}

					if (!success) {
						Cat.logError(new RuntimeException("All cat servers: " + m_catServers.getServers()
						      + "are unreachable. DataEntity: " + dataEntities.toString()));
					}
				} else {
					Thread.sleep(5);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private boolean sendBatchEntities(List<DataEntity> entities) {
		List<String> servers = m_catServers.getServers();

		for (String server : servers) {
			String entityContent = buildBatchEntities(entities);
			String content = "&batch=" + entityContent;

			if (sendData(server, content)) {
				return true;
			}
		}
		return false;
	}

	private boolean sendData(String server, String content) {
		boolean flag = false;
		String url = m_catServers.buildSystemUrl(server);

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
				flag = true;
			}
		} catch (Exception e) {
			Cat.logEvent("DataSender", "Failed", Event.SUCCESS, "server [" + server + "] is not reachable.");
		}
		return flag;
	}

	@Override
	public void shutdown() {
	}
}
