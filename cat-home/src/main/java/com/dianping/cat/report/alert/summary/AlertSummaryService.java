package com.dianping.cat.report.alert.summary;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.AlertSummary;
import com.dianping.cat.home.dal.report.AlertSummaryDao;

public class AlertSummaryService {

	@Inject
	private AlertSummaryDao m_alertSummaryDao;

	public void insert(com.dianping.cat.home.alert.summary.entity.AlertSummary alertSummary) {
		AlertSummary summary = new AlertSummary();
		String content = alertSummary.toString();

		summary.setDomain(alertSummary.getDomain());
		summary.setAlertTime(alertSummary.getAlertDate());
		summary.setContent(content);

		try {
			m_alertSummaryDao.insert(summary);
		} catch (DalException e) {
			Cat.logError("insert alert summary error: " + content, e);
		}
	}

}
