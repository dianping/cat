package com.dianping.cat.status;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;

public class StatusUpdateTask implements Task, Initializable {
	@Inject
	private MessageStatistics m_statistics;

	@Inject
	private ClientConfigManager m_manager;

	private boolean m_active = true;

	private String m_ipAddress;

	private long m_interval = 60 * 1000; // 60 seconds

	private String m_jars;

	private void buildClasspath() {
		ClassLoader loader = StatusUpdateTask.class.getClassLoader();
		StringBuilder sb = new StringBuilder();

		buildClasspath(loader, sb);
		if (sb.length() > 0) {
			m_jars = sb.substring(0, sb.length() - 1);
		}
	}

	private void buildClasspath(ClassLoader loader, StringBuilder sb) {
		if (loader instanceof URLClassLoader) {
			URL[] urLs = ((URLClassLoader) loader).getURLs();
			for (URL url : urLs) {
				String jar = parseJar(url.toExternalForm());

				if (jar != null) {
					sb.append(jar).append(',');
				}
			}
			ClassLoader parent = loader.getParent();

			buildClasspath(parent, sb);
		}
	}

	private void buildExtensionData(StatusInfo status) {
		StatusExtensionRegister res = StatusExtensionRegister.getInstance();
		List<StatusExtension> extensions = res.getStatusExtension();

		for (StatusExtension extension : extensions) {
			String id = extension.getId();
			String des = extension.getDescription();
			Map<String, String> propertis = extension.getProperties();
			Extension item = status.findOrCreateExtension(id).setDescription(des);

			for (Entry<String, String> entry : propertis.entrySet()) {
				try {
					double value = Double.parseDouble(entry.getValue());
					item.findOrCreateExtensionDetail(entry.getKey()).setValue(value);
				} catch (Exception e) {
					Cat.logError("StatusExtension can only be double type", e);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "StatusUpdateTask";
	}

	@Override
	public void initialize() throws InitializationException {
		m_ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	private String parseJar(String path) {
		if (path.endsWith(".jar")) {
			int index = path.lastIndexOf('/');

			if (index > -1) {
				return path.substring(index + 1);
			}
		}
		return null;
	}

	@Override
	public void run() {
		// try to wait cat client init success
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			return;
		}

		while (true) {
			Calendar cal = Calendar.getInstance();
			int second = cal.get(Calendar.SECOND);

			// try to avoid send heartbeat at 59-01 second
			if (second < 2 || second > 58) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore it
				}
			} else {
				break;
			}
		}

		try {
			buildClasspath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MessageProducer cat = Cat.getProducer();
		Transaction reboot = cat.newTransaction("System", "Reboot");

		reboot.setStatus(Message.SUCCESS);
		cat.logEvent("Reboot", NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), Message.SUCCESS, null);
		reboot.complete();

		while (m_active) {
			long start = MilliSecondTimer.currentTimeMillis();

			if (m_manager.isCatEnabled()) {
				Transaction t = cat.newTransaction("System", "Status");
				Heartbeat h = cat.newHeartbeat("Heartbeat", m_ipAddress);
				StatusInfo status = new StatusInfo();

				t.addData("dumpLocked", m_manager.isDumpLocked());
				try {
					StatusInfoCollector statusInfoCollector = new StatusInfoCollector(m_statistics, m_jars);

					status.accept(statusInfoCollector.setDumpLocked(m_manager.isDumpLocked()));

					buildExtensionData(status);
					h.addData(status.toString());
					h.setStatus(Message.SUCCESS);
				} catch (Throwable e) {
					h.setStatus(e);
					cat.logError(e);
				} finally {
					h.complete();
				}
				t.setStatus(Message.SUCCESS);
				t.complete();
			}
			long elapsed = MilliSecondTimer.currentTimeMillis() - start;

			if (elapsed < m_interval) {
				try {
					Thread.sleep(m_interval - elapsed);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	public void setInterval(long interval) {
		m_interval = interval;
	}

	@Override
	public void shutdown() {
		m_active = false;
	}
}
