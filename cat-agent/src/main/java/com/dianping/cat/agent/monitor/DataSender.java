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

import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class DataSender implements Task {

	@Inject
	private EnvironmentConfig m_environmentConfig;

	private static final long DURATION = 60 * 1000;

	private BlockingQueue<DataEntity> m_entityQue = new ArrayBlockingQueue<DataEntity>(5000);

	private List<DataEntity> m_dataEntities = new ArrayList<DataEntity>();

	public boolean put(List<DataEntity> entities) {
		boolean result = true;

		for (DataEntity entity : entities) {
			boolean temp = m_entityQue.offer(entity);

			if (!temp) {
				result = temp;
			}
		}
		return result;
	}

	private String buildBatchEntities(List<DataEntity> entities) {
		StringBuilder sb = new StringBuilder();
		for (DataEntity entity : entities) {
			sb.append(entity.getId()).append("\t").append(entity.getType()).append("\t").append(entity.getValue())
			      .append("\n");
		}
		return sb.toString();
	}

	private boolean sendBatchEntities(List<DataEntity> entities) {
		List<String> servers = m_environmentConfig.getServers();

		for (String server : servers) {
			try {
				String url = buildUrl(server);
				URLConnection conn = new URL(url).openConnection();

				conn.setDoOutput(true);
				conn.setDoInput(true);

				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				String entityContent = buildBatchEntities(m_dataEntities);
				String content = "&batch=" + entityContent;

				writer.write(content);
				writer.flush();

				InputStream in = conn.getInputStream();
				String result = Files.forIO().readFrom(in, "utf-8");

				if (sendOK(result)) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				Cat.logError(e);
			}
			return false;
		}
		return false;
	}

	private boolean sendOK(String content) {
		if (content.contains("{\"statusCode\":\"0\"}")) {
			return true;
		} else {
			return false;
		}
	}

	private String buildUrl(String server) {
		String group = m_environmentConfig.getGroup();
		String domain = m_environmentConfig.getDomain();
		long current = System.currentTimeMillis();

		String urlFormat = "http://%1$s:2281/cat/r/monitor?op=batch&timestamp=%2$s&group=%3$s&domain=%4$s";
		String url = String.format(urlFormat, server, current, group, domain);

		return url;
	}

	@Override
	public void run() {
		while (true) {
			Transaction t = Cat.newTransaction("Data", "Send");

			try {
				long current = System.currentTimeMillis();

				try {
					while (m_entityQue.size() > 0) {
						DataEntity entity = m_entityQue.poll(5, TimeUnit.MILLISECONDS);

						m_dataEntities.add(entity);
					}
					if (!m_dataEntities.isEmpty()) {
						boolean success = sendBatchEntities(m_dataEntities);
						
						if (success) {
							Cat.logEvent("DataSender", "OK");
						} else {
							Cat.logEvent("DataSender", "ERROR");
						}
					}

				} catch (Exception e) {
					Cat.logError(e);
				}
				long duration = System.currentTimeMillis() - current;
				long sleeptime = DURATION - duration;

				if (sleeptime > 0) {
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						break;
					}
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

}
