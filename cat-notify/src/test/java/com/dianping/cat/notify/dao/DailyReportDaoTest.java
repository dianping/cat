package com.dianping.cat.notify.dao;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.notify.BaseTest;
import com.dianping.cat.notify.model.DailyReport;

public class DailyReportDaoTest extends BaseTest {

	@Test
	public void testInsert(){
		DailyReportDao dailyReportDao = (DailyReportDao)super.context.getBean("dailyReportDao");
		long timestamp =  System.currentTimeMillis();
		timestamp = timestamp - 60 * 60 * 1000 * 24 * 3;
        try {
        	Date start = new Date(timestamp);
        	Date end = new Date(timestamp + 60 * 60 * 1000 * 24);
			List<DailyReport> dailyReportList = dailyReportDao.findAllByDomainNameDuration(start, end, "Cat",null, DailyReport.XML_TYPE);
			Assert.assertEquals(true, dailyReportList!=null);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
