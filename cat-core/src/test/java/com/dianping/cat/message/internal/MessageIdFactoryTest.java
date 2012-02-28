package com.dianping.cat.message.internal;

import junit.framework.Assert;

import org.junit.Test;

public class MessageIdFactoryTest {
	private long m_timestamp = 1330327814748L;

	private MessageIdFactory m_factory = new MessageIdFactory() {
		@Override
		protected long getTimestamp() {
			return m_timestamp;
		}
	};

	@Test
	public void test() throws Exception {
		m_factory.initialize();
		
		check("domain1", "domain1-c0a83f99-135bdb7825c-0");
		check("domain1", "domain1-c0a83f99-135bdb7825c-1");
		check("domain1", "domain1-c0a83f99-135bdb7825c-2");
		check("domain1", "domain1-c0a83f99-135bdb7825c-3");

		m_timestamp++;
		check("domain1", "domain1-c0a83f99-135bdb7825d-0");
		check("domain1", "domain1-c0a83f99-135bdb7825d-1");
		check("domain1", "domain1-c0a83f99-135bdb7825d-2");

		m_timestamp++;
		check("domain1", "domain1-c0a83f99-135bdb7825e-0");
		check("domain1", "domain1-c0a83f99-135bdb7825e-1");
		check("domain1", "domain1-c0a83f99-135bdb7825e-2");
	}

	private void check(String domain, String expected) {
		m_factory.setDomain(domain);
		m_factory.setIpAddress("c0a83f99"); // 192.168.63.153

		String actual = m_factory.getNextId();

		Assert.assertEquals(expected, actual);
	}
}
