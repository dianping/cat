package com.dianping.cat.message.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dianping.cat.configuration.model.entity.Config;

public class MessageIdFactory {
	private long m_lastTimestamp = getTimestamp();

	private volatile int m_index;

	private String m_domain;

	private String m_ipAddress;

	public MessageId getNextId() {
		long timestamp = getTimestamp();
		int index;

		synchronized (this) {
			if (timestamp != m_lastTimestamp) {
				m_index = 0;
				m_lastTimestamp = timestamp;
			}

			index = m_index++;
		}

		return new MessageId(m_domain, m_ipAddress, timestamp, index);
	}

	protected long getTimestamp() {
		return MilliSecondTimer.currentTimeMillis();
	}

	public void initialize(Config clientConfig) {
		try {
			m_domain = clientConfig.getApp().getDomain();
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
