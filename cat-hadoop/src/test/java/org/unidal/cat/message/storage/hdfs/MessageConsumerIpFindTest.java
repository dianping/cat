package org.unidal.cat.message.storage.hdfs;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;

public class MessageConsumerIpFindTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		ServerConfigManager config = lookup(ServerConfigManager.class);

		config.initialize(new File(MessageConsumerIpFindTest.class.getClassLoader().getResource("server.xml").getFile()));
	}

	@Test
	public void test() {
		MessageConsumerFinder find = lookup(MessageConsumerFinder.class, "hdfs");
		MessageId id = MessageId.parse("shop-web-0a420d56-405915-16");
		Set<String> ips = find.findConsumerIps(id.getDomain(),id.getHour());

		System.err.println(ips);

	}
}
