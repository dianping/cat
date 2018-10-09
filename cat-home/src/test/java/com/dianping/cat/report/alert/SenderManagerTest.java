package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;

public class SenderManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		SenderManager manager = lookup(SenderManager.class);

		List<String> receivers = new ArrayList<String>();
		SendMessageEntity message = new SendMessageEntity("group", "title11", "type", "content22", receivers);

		receivers.add("yong.you@dianping.com");
		receivers.add("yong.you2@dianping.com");
		manager.sendAlert(AlertChannel.MAIL, message);
		
		
		receivers.clear();
		receivers.add("18616671676");
		manager.sendAlert(AlertChannel.SMS, message);
	}

}
