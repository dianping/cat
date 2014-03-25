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
		String title ="业务告警, 产品线[支付], 业务指标[创建订单]";
		content = content +content;
		boolean result = mailsms.sendEmail(title, content, emails);
		Assert.assertEquals(true, result);
		
		Thread.sleep(1000*5);
	}
	
	@Test
	public void testSendSms() throws Exception {
		MailSMS mailsms = lookup(MailSMS.class);
		List<String> phones =new ArrayList<String>();

		phones.add("18721960052");
		mailsms.sendSms("[MetricAlert] [业务告警] 产品线[团购] 业务指标[成功验券]", "sdf", phones);
		Thread.sleep(1000*5);
	}
	
}
