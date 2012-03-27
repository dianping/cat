package com.dianping.cat.status;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.LocalIP;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.site.lookup.annotation.Inject;

public class StatusUpdateTask implements Runnable, Initializable {
	@Inject
	private MessageStatistics m_statistics;

	private boolean m_active = true;

	private String m_ipAddress;

	private long m_interval = 1000; // 1 ms

	@Override
	public void initialize() throws InitializationException {
			m_ipAddress = LocalIP.getAddress();
	}

	@Override
	public void run() {
		while (m_active) {
			long start = MilliSecondTimer.currentTimeMillis();
			Heartbeat heartbeat = Cat.getProducer().newHeartbeat("Heartbeat", m_ipAddress);
			StatusInfo status = new StatusInfo();

			status.accept(new StatusInfoCollector(m_statistics));

			heartbeat.addData(status.toString());
			heartbeat.setStatus(Message.SUCCESS);
			heartbeat.complete();

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

	public void shutdown() {
		m_active = false;
	}
}
