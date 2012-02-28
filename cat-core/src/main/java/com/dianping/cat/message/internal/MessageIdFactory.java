package com.dianping.cat.message.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageManager;
import com.site.lookup.annotation.Inject;

public class MessageIdFactory implements Initializable {
	@Inject
	private MessageManager m_manager;

	private long m_lastTimestamp = getTimestamp();

	private volatile int m_index;

	private String m_domain;

	private String m_ipAddress;

	public String getNextId() {
		StringBuilder sb = new StringBuilder(40);
		long timestamp = getTimestamp();
		int index;

		synchronized (this) {
			if (timestamp != m_lastTimestamp) {
				m_index = 0;
				m_lastTimestamp = timestamp;
			}

			index = m_index++;
		}

		sb.append(m_domain); // in one machine, domain would not change
		sb.append('-');
		sb.append(m_ipAddress); // in one machine, ip address would not change
		sb.append('-');
		sb.append(Long.toHexString(timestamp));
		sb.append('-');
		sb.append(Integer.toHexString(index));

		return sb.toString();
	}

	protected long getTimestamp() {
		return MilliSecondTimer.currentTimeMillis();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			m_domain = m_manager.getClientConfig().getApp().getDomain();
			m_ipAddress = m_manager.getClientConfig().getApp().getIp();
		} catch (Exception e) {
			// ignore it
		}

		if (m_ipAddress == null) {
			try {
				byte[] bytes = InetAddress.getLocalHost().getAddress();
				StringBuilder sb = new StringBuilder(bytes.length / 2);

				for (byte b : bytes) {
					sb.append(Integer.toHexString((b >> 4) & 0x0F));
					sb.append(Integer.toHexString(b & 0x0F));
				}

				m_ipAddress = sb.toString();
			} catch (UnknownHostException e) {
				// ignore it
			}
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
}
