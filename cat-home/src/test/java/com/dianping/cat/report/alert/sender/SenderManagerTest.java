package com.dianping.cat.report.alert.sender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.webres.helper.Files;

import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;

public class SenderManagerTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		SenderManager manager = lookup(SenderManager.class);
		List<String> receivers = new ArrayList<String>();

		receivers.add("yong.you@dianping.com");
        AlertMessageEntity message = new AlertMessageEntity("Test", "test", "test", "content", receivers);
		boolean result = manager.sendAlert(AlertChannel.MAIL, message);

		System.out.println(result);
	}

}
