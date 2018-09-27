package com.dianping.cat.report.alert.sender;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;

public class SenderManagerTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
	}
	
	@Test
	public void test() throws Exception {
		SenderManager manager = lookup(SenderManager.class);
		List<String> receivers = new ArrayList<String>();

		receivers.add("yong.you@dianping.com");
		SendMessageEntity message = new SendMessageEntity("Test", "test", "title", "content", receivers);
		boolean result = manager.sendAlert(AlertChannel.MAIL, message);

		System.out.println(result);
	}

}
