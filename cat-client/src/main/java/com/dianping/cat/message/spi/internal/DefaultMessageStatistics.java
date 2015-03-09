package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageStatistics implements MessageStatistics {
	private long m_produced;

	private long m_overflowed;

	private long m_bytes;

	@Override
	public long getBytes() {
		return m_bytes;
	}

	@Override
	public long getOverflowed() {
		return m_overflowed;
	}

	@Override
	public long getProduced() {
		return m_produced;
	}

	@Override
	public void onBytes(int bytes) {
		m_bytes += bytes;
		m_produced++;
	}

	@Override
	public void onOverflowed(MessageTree tree) {
		m_overflowed++;
	}
}
