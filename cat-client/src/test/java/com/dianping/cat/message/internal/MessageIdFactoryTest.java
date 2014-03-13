package com.dianping.cat.message.internal;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class MessageIdFactoryTest {
	private long m_timestamp = 1330327814748L;

	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private MessageIdFactory m_factory = new MessageIdFactory() {
		@Override
		protected long getTimestamp() {
			return m_timestamp;
		}
	};

	@Test
	public void test() {
		String id = "UNKNOWN-c0a82050-376665-314";
		MessageId message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals(2, message.getVersion());
		Assert.assertEquals(id, message.toString());

		id = "ARCH-UNKNOWN-c0a82050-376665-314";
		message = MessageId.parse(id);

		Assert.assertEquals(1355994000000L, message.getTimestamp());
		Assert.assertEquals("192.168.32.80", message.getIpAddress());
		Assert.assertEquals(2, message.getVersion());
		Assert.assertEquals("ARCH-UNKNOWN", message.getDomain());
		Assert.assertEquals(id, message.toString());

	}

	private void check(String domain, String expected) {
		m_factory.setDomain(domain);
		m_factory.setIpAddress("c0a83f99"); // 192.168.63.153

		String actual = m_factory.getNextId().toString();

		Assert.assertEquals(expected, actual);

		MessageId id = MessageId.parse(actual);

		Assert.assertEquals(domain, id.getDomain());
		Assert.assertEquals("c0a83f99", id.getIpAddressInHex());
		Assert.assertEquals(m_timestamp, id.getTimestamp());
	}

	@Test
	public void testNextId() throws Exception {
		m_factory.initialize("test");

		check("domain1", "domain1-c0a83f99-1330327814748-0");
		check("domain1", "domain1-c0a83f99-1330327814748-1");
		check("domain1", "domain1-c0a83f99-1330327814748-2");
		check("domain1", "domain1-c0a83f99-1330327814748-3");

		m_timestamp++;
		check("domain1", "domain1-c0a83f99-1330327814749-0");
		check("domain1", "domain1-c0a83f99-1330327814749-1");
		check("domain1", "domain1-c0a83f99-1330327814749-2");

		m_timestamp++;
		check("domain1", "domain1-c0a83f99-1330327814750-0");
		check("domain1", "domain1-c0a83f99-1330327814750-1");
		check("domain1", "domain1-c0a83f99-1330327814750-2");
	}

	@Test
	public void testNextIdContinousIncrement() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("test");

		String id1 = f1.getNextId();
		String id2 = f1.getNextId();

		f1.close();

		MessageIdFactory f2 = new MessageIdFactory();

		f2.initialize("test");

		String id3 = f2.getNextId();
		String id4 = f2.getNextId();

		// f2.close();

		Assert.assertEquals(false, id1.equals(id2));
		Assert.assertEquals(false, id3.equals(id4));

		Assert.assertEquals(false, id1.equals(id3));
		Assert.assertEquals(false, id2.equals(id4));
	}

	@Test(timeout = 500)
	public void test_performance() throws IOException {
		MessageIdFactory f1 = new MessageIdFactory();

		f1.initialize("test");

		for (int i = 0; i < 10000; i++) {
			f1.getNextId();
		}
	}

	@Test
	public void testToHexString() {
		checkHexString(0, "0");
		checkHexString(m_timestamp++, "135bdb7825c");
		checkHexString(m_timestamp++, "135bdb7825d");
		checkHexString(m_timestamp++, "135bdb7825e");
		checkHexString(m_timestamp++, "135bdb7825f");
		checkHexString(m_timestamp++, "135bdb78260");
	}

	@Test
	public void testGetIpAddress() {
		for (int i = 0; i < 1000000; i++) {
			MessageId id = new MessageId(null, "ffffffff", m_timestamp, 0);

			Assert.assertEquals("255.255.255.255", id.getIpAddress());

			id = new MessageId(null, "11f111f1", m_timestamp, 0);

			Assert.assertEquals("17.241.17.241", id.getIpAddress());
		}
	}

	private void checkHexString(long value, String expected) {
		StringBuilder sb = new StringBuilder();

		toHexString(sb, value);

		String hex = sb.toString();

		Assert.assertEquals(Long.toHexString(value), hex);
		Assert.assertEquals(expected, hex);
	}

	void toHexString(StringBuilder sb, long value) {
		int offset = sb.length();

		do {
			int index = (int) (value & 0x0F);

			sb.append(digits[index]);
			value >>>= 4;
		} while (value != 0);

		int len = sb.length();

		while (offset < len) {
			char ch1 = sb.charAt(offset);
			char ch2 = sb.charAt(len - 1);

			sb.setCharAt(offset, ch2);
			sb.setCharAt(len - 1, ch1);
			offset++;
			len--;
		}
	}
}
