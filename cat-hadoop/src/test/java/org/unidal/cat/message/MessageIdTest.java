package org.unidal.cat.message;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;

public class MessageIdTest extends ComponentTestCase {
	@Test
	public void test() throws IOException {
		MessageId id = MessageId.parse("child-0a260015-403899-12345");

		Assert.assertEquals("child", id.getDomain());
		Assert.assertEquals("0a260015", id.getIpAddressInHex());
		Assert.assertEquals("10.38.0.21", id.getIpAddress());
		Assert.assertEquals(170262549, id.getIpAddressValue());
		Assert.assertEquals(403899, id.getHour());
		Assert.assertEquals(1454036400000L, id.getTimestamp());
		Assert.assertEquals(12345, id.getIndex());

	}
	
	@Test
	public void testDomain() throws IOException {
		MessageId id = MessageId.parse("child-child-child-0a260015-403899-12345");

		Assert.assertEquals("child-child-child", id.getDomain());
		Assert.assertEquals("0a260015", id.getIpAddressInHex());
		Assert.assertEquals("10.38.0.21", id.getIpAddress());
		Assert.assertEquals(170262549, id.getIpAddressValue());
		Assert.assertEquals(403899, id.getHour());
		Assert.assertEquals(1454036400000L, id.getTimestamp());
		Assert.assertEquals(12345, id.getIndex());
	}
}
