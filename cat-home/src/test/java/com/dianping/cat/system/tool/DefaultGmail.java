package com.dianping.cat.system.tool;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class DefaultGmail extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		MailSMS mailsms = lookup(MailSMS.class);
		List<String> emails = new ArrayList<String>();

		emails.add("yong.you@dianping.com");
		emails.add("youyong205@126.com");

		boolean result = mailsms.sendEmail("ts", "ts", emails);
		Assert.assertEquals(true, result);
	}

}
