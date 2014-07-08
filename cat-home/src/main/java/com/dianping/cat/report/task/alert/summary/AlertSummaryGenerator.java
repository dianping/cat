package com.dianping.cat.report.task.alert.summary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

public class AlertSummaryGenerator {

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private TopologyGraphManager m_topologyManager;

	// fetch alerts during this period, time unit is ms, default value is 1 hour
	private final long DURATION = 60 * 60 * 1000L;

	private com.dianping.cat.home.alert.summary.entity.Alert convertToAlert(Alert dbAlert) {
		com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();

		alert.setAlertTime(dbAlert.getAlertTime());
		alert.setContext(dbAlert.getContent());
		alert.setMetric(dbAlert.getMetric());
		alert.setType(dbAlert.getType());

		return alert;
	}

	private com.dianping.cat.home.alert.summary.entity.Alert convertToDependAlert(String domain, Alert dbAlert) {
		com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();

		alert.setAlertTime(dbAlert.getAlertTime());
		alert.setContext(dbAlert.getContent());
		alert.setMetric(domain + ":" + dbAlert.getMetric());
		alert.setType(dbAlert.getType());

		return alert;
	}

	public AlertSummary generateAlertSummary(String domain, Date date) {
		AlertSummary alertSummary = new AlertSummary();

		alertSummary.setDomain(domain);
		alertSummary.setAlertDate(date);

		alertSummary.addCategory(generateCategoryByTimeCategory(date, "network"));
		alertSummary.addCategory(generateCategoryByTimeCateDomain(date, "business", domain));
		alertSummary.addCategory(generateCategoryByTimeCateDomain(date, "exception", domain));
		alertSummary.addCategory(generateCategoryByTimeCateDomain(date, "system", domain));

		List<String> dependencyDomains = queryDependencyDomains(date, domain);
		alertSummary.addCategory(generateDependCategoryByTimeCateDomain(date, "business", dependencyDomains));
		alertSummary.addCategory(generateDependCategoryByTimeCateDomain(date, "exception", dependencyDomains));

		return alertSummary;
	}

	private Category generateCategoryByTimeCateDomain(Date date, String cate, String domain) {
		Category category = new Category(cate);
		String dbCategoryName = cate + "-alert";
		Date startTime = new Date(date.getTime() - DURATION / 2);
		Date endTime = new Date(date.getTime() + DURATION / 2);

		try {
			List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategoryDomain(startTime, endTime, dbCategoryName, domain,
			      AlertEntity.READSET_FULL);
			setDBAlertsToCategory(category, dbAlerts);
		} catch (DalException e) {
			Cat.logError("find alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
		}

		return category;
	}

	private Category generateCategoryByTimeCategory(Date date, String cate) {
		Category category = new Category(cate);
		String dbCategoryName = cate + "-alert";
		Date startTime = new Date(date.getTime() - DURATION / 2);
		Date endTime = new Date(date.getTime() + DURATION / 2);

		try {
			List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategory(startTime, endTime, dbCategoryName,
			      AlertEntity.READSET_FULL);
			setDBAlertsToCategory(category, dbAlerts);
		} catch (DalException e) {
			Cat.logError("find alerts error for category:" + cate + " date:" + date, e);
		}

		return category;
	}

	private Category generateDependCategoryByTimeCateDomain(Date date, String cate, List<String> dependencyDomains) {
		String categoryName = "dependency-" + cate;
		String dbCategoryName = cate + "-alert";
		Category category = new Category(categoryName);
		Date startTime = new Date(date.getTime() - DURATION / 2);
		Date endTime = new Date(date.getTime() + DURATION / 2);

		for (String domain : dependencyDomains) {
			try {
				List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategoryDomain(startTime, endTime, dbCategoryName,
				      domain, AlertEntity.READSET_FULL);
				setDBAlertsToDependCategory(category, domain, dbAlerts);
			} catch (DalException e) {
				Cat.logError("find dependency alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
			}
		}

		return category;
	}

	private List<String> queryDependencyDomains(Date date, String domain) {
		List<String> domains = new ArrayList<String>();
		TopologyGraph topology = m_topologyManager.buildTopologyGraph(domain, date.getTime());

		for (String dependencyDomain : topology.getNodes().keySet()) {
			domains.add(dependencyDomain);
		}

		return domains;
	}

	private void setDBAlertsToCategory(Category category, List<Alert> dbAlerts) {
		for (Alert dbAlert : dbAlerts) {
			category.addAlert(convertToAlert(dbAlert));
		}
	}

	private void setDBAlertsToDependCategory(Category category, String domain, List<Alert> dbAlerts) {
		for (Alert dbAlert : dbAlerts) {
			category.addAlert(convertToDependAlert(domain, dbAlert));
		}
	}

}
