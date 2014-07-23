package com.dianping.cat.report.task.alert;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;

public class AlertManager {

	@Inject
	protected AlertDao m_alertDao;

	private Alert buildAlert(String categoryName, String domainName, String metricName, String mailTitle,
	      AlertResultEntity alertResult) {
		Alert alert = new Alert();

		alert.setDomain(domainName);
		alert.setAlertTime(alertResult.getAlertTime());
		alert.setCategory(categoryName);
		alert.setType(alertResult.getAlertType());
		alert.setContent(mailTitle + "<br/>" + alertResult.getContent());
		alert.setMetric(metricName);

		return alert;
	}

	protected void storeAlert(String categoryName, String domainName, String metricName, String mailTitle,
	      AlertResultEntity alertResult) {
		Alert alert = buildAlert(categoryName, domainName, metricName, mailTitle, alertResult);

		try {
			int count = m_alertDao.insert(alert);

			if (count != 1) {
				Cat.logError("insert alert error: " + alert.toString(), new RuntimeException());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
}
