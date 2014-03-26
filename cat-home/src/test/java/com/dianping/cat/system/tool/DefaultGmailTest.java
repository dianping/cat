package com.dianping.cat.system.tool;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class DefaultGmailTest extends ComponentTestCase {

	@Test
	public void testSendMail() throws Exception {
		MailSMS mailsms = lookup(MailSMS.class);
		List<String> emails = new ArrayList<String>();
		emails.add("yong.you@dianping.com");

		String content = "[业务告警] [产品线 团购][业务指标 成功验券][告警时间 2014-03-26 09:11:47]";
		String title = "[业务告警] [产品线 团购][业务指标 成功验券][告警时间 2014-03-26 09:11:47]";
		content = content + content;

		boolean result1 = mailsms.sendEmail(title, content, emails);
		boolean result2 = ((DefaultMailImpl) mailsms).sendEmailInternal(title, content, emails);
		Assert.assertEquals(true, result1);
		Assert.assertEquals(true, result2);

		Thread.sleep(1000 * 5);
	}

	@Test
	public void testSendSms() throws Exception {
		MailSMS mailsms = lookup(MailSMS.class);
		List<String> phones = new ArrayList<String>();

		phones.add("18721960052");
		mailsms.sendSms("[MetricAlert] [业务告警] 产品线[团购] 业务指标[成功验券]", "sdf", phones);
		Thread.sleep(1000 * 5);
	}

}
