package com.dianping.cat.message.internal;

import java.util.List;

import com.site.helper.Splitters;

public class MessageId {
	private static final long VERSION1_THRESHOLD = 1325347200000L; // Jan. 1, 2012

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

	public static MessageId parse(String messageId) {
		List<String> list = Splitters.by('-').split(messageId);
		int len = list.size();

		if (len >= 4) {
			String ipAddressInHex = list.get(len - 3);
			long timestamp = Long.parseLong(list.get(len - 2));
			int index = Integer.parseInt(list.get(len - 1));
			String domain;

			if (len > 4) { // allow domain contains '-'
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < len - 3; i++) {
					if (i > 0) {
						sb.append('-');
					}

					sb.append(list.get(i));
				}

				domain = sb.toString();
			} else {
				domain = list.get(0);
			}

			return new MessageId(domain, ipAddressInHex, timestamp, index);
		}

		throw new RuntimeException("Invalid message id format: " + messageId);
	}

	public String getDomain() {
		return m_domain;
	}

	public int getIndex() {
		return m_index;
	}

	public String getIpAddressInHex() {
		return m_ipAddressInHex;
	}

	public long getTimestamp() {
		if (m_timestamp > VERSION1_THRESHOLD) {
			return m_timestamp;
		} else {
			return m_timestamp * 3600 * 1000L;
		}
	}

	public int getVersion() {
		if (m_timestamp > VERSION1_THRESHOLD) {
			return 1;
		} else {
			return 2;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(m_domain);
		sb.append('-');
		sb.append(m_ipAddressInHex);
		sb.append('-');
		sb.append(m_timestamp);
		sb.append('-');
		sb.append(m_index);

		return sb.toString();
	}
}
