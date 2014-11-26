package com.dianping.cat.status;

import java.util.Calendar;
import java.util.List;

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

	private void buildExtensionData(StatusInfo status) {
		StatusExtensionRegister res = StatusExtensionRegister.getInstance();
		List<Extension> extensions = res.geteExtensions();

		for (Extension extension : extensions) {
			status.addExtension(extension);
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
					StatusInfoCollector statusInfoCollector = new StatusInfoCollector(m_statistics);

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
