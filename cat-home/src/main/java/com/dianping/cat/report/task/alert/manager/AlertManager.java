package com.dianping.cat.report.task.alert.manager;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class AlertManager {

	@Inject
	private AlertDao m_alertDao;

	private Alert buildAlert(String categoryName, String domainName, String metricName, String mailTitle,
	      AlertResultEntity alertResult) {
		Alert alert = new Alert();

		alert.setDomain(domainName);
		alert.setAlertTime(alertResult.getAlertTime());
		alert.setCategory(categoryName);
		alert.setType(alertResult.getAlertLevel());
		alert.setContent(mailTitle + "<br/>" + alertResult.getContent());
		alert.setMetric(metricName);

		return alert;
	}

	private Alert buildAlert(AlertEntity alertEntity, AlertMessageEntity message) {
		Alert alert = new Alert();

		alert.setDomain(alertEntity.getGroup());
		alert.setAlertTime(alertEntity.getAlertDate());
		alert.setCategory(alertEntity.getDbType());
		alert.setType(alertEntity.getType());
		alert.setContent(message.getTitle() + "<br/>" + message.getContent());
		alert.setMetric(alertEntity.getMetricName());

		return alert;
	}

	public void storeAlert(String categoryName, String domainName, String metricName, String mailTitle,
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

	public void storeAlert(AlertEntity alertEntity, AlertMessageEntity message) {
		Alert alert = buildAlert(alertEntity, message);

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
