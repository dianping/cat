package com.dianping.cat.report.service;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.impl.ReportServiceImpl;

public class ReportServiceImplTest extends ComponentTestCase{

	private ReportServiceImpl m_impl = new ReportServiceImpl();

	@Test
	public void test() {
		Date currentDay = TimeUtil.getCurrentDay();
		Date currentWeek = TimeUtil.getCurrentWeek();
		
		Date lastMonth =TimeUtil.getLastMonth();
		Date currentMonth = TimeUtil.getCurrentMonth();

		Calendar cal = Calendar.getInstance();
		cal.setTime(currentMonth);
		cal.add(Calendar.MONTH, 1);
		Date nextMonth = cal.getTime();

		int type = m_impl.getQueryType(currentDay, new Date(currentDay.getTime() + TimeUtil.ONE_DAY));
		Assert.assertEquals(ReportServiceImpl.s_daily, type);

		type = m_impl.getQueryType(currentDay, new Date(currentDay.getTime() + TimeUtil.ONE_HOUR));
		Assert.assertEquals(ReportServiceImpl.s_hourly, type);
		
		type = m_impl.getQueryType(currentWeek, new Date(currentWeek.getTime() + 7 * TimeUtil.ONE_DAY));
		Assert.assertEquals(ReportServiceImpl.s_currentWeekly, type);

		type = m_impl.getQueryType(new Date(currentWeek.getTime() - 7 * TimeUtil.ONE_DAY),
		      new Date(currentWeek.getTime()));
		Assert.assertEquals(ReportServiceImpl.s_historyWeekly, type);
		
		type = m_impl.getQueryType(currentMonth,nextMonth);
		Assert.assertEquals(ReportServiceImpl.s_currentMonth, type);
		
		type = m_impl.getQueryType(lastMonth,currentMonth);
		Assert.assertEquals(ReportServiceImpl.s_historyMonth, type);

		type = m_impl.getQueryType(currentWeek, new Date(currentWeek.getTime() + 8 * TimeUtil.ONE_DAY));
		Assert.assertEquals(ReportServiceImpl.s_customer, type);
		
		type = m_impl.getQueryType(new Date(currentWeek.getTime() + TimeUtil.ONE_DAY), new Date(currentWeek.getTime() + 8 * TimeUtil.ONE_DAY));
		Assert.assertEquals(ReportServiceImpl.s_customer, type);

		
		String m_reportType="month";
		long m_date =TimeUtil.getCurrentMonth().getTime();
		long temp = 0;
		 cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		if ("month".equals(m_reportType)) {
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			System.out.println(maxDay);
			temp = m_date + maxDay * (TimeUtil.ONE_HOUR * 24);
			
			System.out.println(new Date(temp));
		} else if ("week".equals(m_reportType)) {
			temp = m_date + 7 * (TimeUtil.ONE_HOUR * 24);
		} else {
			temp = m_date + (TimeUtil.ONE_HOUR * 24);
		}
		cal.setTimeInMillis(temp);
	}
}
