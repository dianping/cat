package com.dianping.cat.message.internal;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.encoder.PlainTextMetricBagEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class DefaultMetricBag implements MetricBag {
	private String m_domain;

	private String m_hostName;

	private String m_ipAddress;

	private List<Metric> m_metrics = new ArrayList<>();

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public String getHostName() {
		return m_hostName;
	}

	@Override
	public String getIpAddress() {
		return m_ipAddress;
	}

	public List<Metric> getMetrics() {
		return m_metrics;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setHostName(String hostName) {
		m_hostName = hostName;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	@Override
	public String toString() {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(2 * 1024); // 2K

		new PlainTextMetricBagEncoder().encode(this, buf);

		return buf.toString(Charset.forName("utf-8"));
	}
}
