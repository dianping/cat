package com.dianping.cat.notify.dao;

import java.util.Date;
import java.util.List;

import com.dianping.cat.notify.model.DailyReport;

public interface DailyReportDao {

	public List<DailyReport> findAllByDomainNameDuration(Date startDate, Date endDate, String domain, String name, int type) throws Exception;

	public List<DailyReport> findSendMailReportDomainDuration(Date startDate, Date endDate, String domain, int type) throws Exception;

	
}
