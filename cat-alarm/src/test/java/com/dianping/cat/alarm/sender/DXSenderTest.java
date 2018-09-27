package com.dianping.cat.alarm.sender;

import java.util.Arrays;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.sender.DXSender;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;

public class DXSenderTest extends ComponentTestCase {

	@Test
	public void test() {
		DXSender dxSender = new DXSender();
		try {
			dxSender.initialize();
		} catch (InitializationException e) {
			e.printStackTrace();
		}
		SendMessageEntity message = new SendMessageEntity("test", "this is a title.", "test", "this is a content",
		      Arrays.asList("jialin.sun@dianping.com"));

		dxSender.send(message);
	}
}
