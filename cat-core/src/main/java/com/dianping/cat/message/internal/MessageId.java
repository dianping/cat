package com.dianping.cat.message.internal;

import java.util.List;

import com.site.helper.Splitters;

public class MessageId {
	private String m_domain;

	private String m_ipAddressInHex;

	private long m_timestamp;

	private int m_index;

	MessageId(String domain, String ipAddressInHex, long timestamp, int index) {
		m_domain = domain;
		m_ipAddressInHex = ipAddressInHex;
		m_timestamp = timestamp;
		m_index = index;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getIpAddressInHex() {
		return m_ipAddressInHex;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public int getIndex() {
		return m_index;
	}

	public static MessageId parse(String messageId) {
		List<String> list = Splitters.by('-').split(messageId);
		int len = list.size();

		if (len == 4) {
			String domain = list.get(0);
			String ipAddressInHex = list.get(1);
			long timestamp = Long.parseLong(list.get(2), 16);
			int index = Integer.parseInt(list.get(3), 16);

			return new MessageId(domain, ipAddressInHex, timestamp, index);
		}

		throw new RuntimeException("Invalid message id format: " + messageId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(m_domain);
		sb.append('-');
		sb.append(m_ipAddressInHex);
		sb.append('-');
		sb.append(Long.toHexString(m_timestamp));
		sb.append('-');
		sb.append(Integer.toHexString(m_index));

		return sb.toString();
	}
}
