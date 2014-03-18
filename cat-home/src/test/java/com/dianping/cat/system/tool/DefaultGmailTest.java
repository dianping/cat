package com.dianping.cat.system.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.eunit.helper.Files;
import org.unidal.lookup.ComponentTestCase;

public class DefaultGmailTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		MailSMS mailsms = lookup(MailSMS.class);
		List<String> emails = new ArrayList<String>();
		emails.add("yong.you@dianping.com");

		String content = Files.forIO().readFrom(new File("/data/applogs/cat/cat_20140313.log"), "utf-8");
		boolean result = mailsms.sendEmail("title", content, emails);
		Assert.assertEquals(true, result);
		
		Thread.sleep(1000*5);
	}

}
