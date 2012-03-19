package com.dianping.cat.message.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MessageIdFactory {
	private long m_lastTimestamp = getTimestamp();

	private volatile int m_index;

	private String m_domain;

	private String m_ipAddress;

	public String getNextId() {
		long timestamp = getTimestamp();
		int index;

		synchronized (this) {
			if (timestamp != m_lastTimestamp) {
				m_index = 0;
				m_lastTimestamp = timestamp;
			}

			index = m_index++;
		}

		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(m_domain);
		sb.append('-');
		sb.append(m_ipAddress);
		sb.append('-');
		sb.append(timestamp);
		sb.append('-');
		sb.append(index);

		return sb.toString();
	}

	protected long getTimestamp() {
		return MilliSecondTimer.currentTimeMillis();
	}

	public void initialize(String domain) {
		m_domain = domain;

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
