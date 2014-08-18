package com.dianping.cat.report.alert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.spliter.SpliterManager;

public class SplitManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		SpliterManager manager = lookup(SpliterManager.class);

		String content1 = manager.process("test<br/>", AlertChannel.MAIL);
		String content2 = manager.process("test<br/>", AlertChannel.SMS);
		String content3 = manager.process("test<br/>", AlertChannel.WEIXIN);
		
		System.out.println(content1);
		System.out.println(content2);
		System.out.println(content3);
	}

}
