package com.dianping.cat.report.service.impl;

import java.util.Date;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.service.AbstractReportService;

public class TransactionReportService extends AbstractReportService<TransactionReport> {

	@Override
	public TransactionReport queryHourlyReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public TransactionReport queryDailyReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public TransactionReport queryWeeklyReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public TransactionReport queryMonthlyReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
   public TransactionReport queryCurrentWeeklyReport(String domain, Date start, Date end) {
	   // TODO Auto-generated method stub
	   return null;
   }

	@Override
   public TransactionReport queryCurrentMonthlyReport(String domain, Date start, Date end) {
	   // TODO Auto-generated method stub
	   return null;
   }

}
