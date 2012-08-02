package com.dianping.cat.notify.dao;

import org.junit.Test;

import com.dianping.cat.notify.BaseTest;
import com.dianping.cat.notify.model.MailLog;

public class MailLogDaoTest extends BaseTest {
	
	@Test
	public void testInsert(){
		MailLogDao mailLogDao = (MailLogDao)super.context.getBean("mailLogDao");
		MailLog mailLog = new MailLog();
		mailLog.setAddress("address");
		mailLog.setCc("cc");
		mailLog.setContent("context");
		mailLog.setError("error");
		mailLog.setStatus(MailLog.SEND_SUCCSS);
		mailLog.setTitle("Title");
		try {
			mailLogDao.insertMailLog(mailLog);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mailLog = null;
	}

}
