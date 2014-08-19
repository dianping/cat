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
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

public class AlertSummaryGenerator {

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private TopologyGraphManager m_topologyManager;

	// fetch alerts during this period, time unit is ms, default value is 5 minnutes
	private final long DURATION = 5 * 60 * 1000L;

	private com.dianping.cat.home.alert.summary.entity.Alert convertToAlert(TopologyEdge edge, Date date) {
		com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();

		alert.setAlertTime(date);
		alert.setContext(edge.getDes());
		alert.setMetric(edge.getKey());
		alert.setType("slow " + edge.getType());
		alert.setDomain(edge.getSelf());

		return alert;
	}

	private com.dianping.cat.home.alert.summary.entity.Alert convertToAlert(Alert dbAlert) {
		com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();

		alert.setAlertTime(dbAlert.getAlertTime());
		alert.setContext(dbAlert.getContent());
		alert.setMetric(dbAlert.getMetric());
		alert.setType(dbAlert.getType());

		return alert;
	}

	private com.dianping.cat.home.alert.summary.entity.Alert convertToAlertWithDomain(Alert dbAlert) {
		com.dianping.cat.home.alert.summary.entity.Alert alert = new com.dianping.cat.home.alert.summary.entity.Alert();

		alert.setAlertTime(dbAlert.getAlertTime());
		alert.setContext(dbAlert.getContent());
		alert.setMetric(dbAlert.getMetric());
		alert.setType(dbAlert.getType());
		alert.setDomain(dbAlert.getDomain());

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

		TopologyGraph topology = m_topologyManager.buildTopologyGraph(domain, date.getTime());
		int statusThreshold = 2;

		alertSummary.addCategory(generateDependCategoryByTopology(date, "business", topology, statusThreshold));

		List<String> dependencyDomains = queryDependencyDomains(topology, date, domain);
		alertSummary.addCategory(generateDependCategoryByTimeCateDomain(date, "exception", dependencyDomains));

		return alertSummary;
	}

	private Category generateCategoryByTimeCategory(Date date, String cate) {
		Category category = new Category(cate);
		String dbCategoryName = cate;
		Date startTime = new Date(date.getTime() - DURATION);

		try {
			List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategory(startTime, date, dbCategoryName,
			      AlertEntity.READSET_FULL);
			setDBAlertsToCategoryWithDomain(category, dbAlerts);
		} catch (DalException e) {
			Cat.logError("find alerts error for category:" + cate + " date:" + date, e);
		}

		return category;
	}

	private Category generateCategoryByTimeCateDomain(Date date, String cate, String domain) {
		Category category = new Category(cate);
		String dbCategoryName = cate;
		Date startTime = new Date(date.getTime() - DURATION);

		try {
			List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategoryDomain(startTime, date, dbCategoryName, domain,
			      AlertEntity.READSET_FULL);
			setDBAlertsToCategory(category, dbAlerts);
		} catch (DalException e) {
			Cat.logError("find alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
		}

		return category;
	}

	private Category generateDependCategoryByTimeCateDomain(Date date, String cate, List<String> dependencyDomains) {
		String categoryName = "dependency_" + cate;
		String dbCategoryName = cate;
		Category category = new Category(categoryName);
		Date startTime = new Date(date.getTime() - DURATION);

		for (String domain : dependencyDomains) {
			try {
				List<Alert> dbAlerts = m_alertDao.queryAlertsByTimeCategoryDomain(startTime, date, dbCategoryName, domain,
				      AlertEntity.READSET_FULL);
				setDBAlertsToCategoryWithDomain(category, dbAlerts);
			} catch (DalException e) {
				Cat.logError("find dependency alerts error for category:" + cate + " domain:" + domain + " date:" + date, e);
			}
		}

		return category;
	}

	private Category generateDependCategoryByTopology(Date date, String cate, TopologyGraph topology, int statusThreshold) {
		String categoryName = "dependency_" + cate;
		Category category = new Category(categoryName);

		for (TopologyEdge edge : topology.getEdges().values()) {
			if (edge.getStatus() >= statusThreshold) {
				category.addAlert(convertToAlert(edge, date));
			}
		}

		return category;
	}

	private List<String> queryDependencyDomains(TopologyGraph topology, Date date, String domain) {
		List<String> domains = new ArrayList<String>();

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

	private void setDBAlertsToCategoryWithDomain(Category category, List<Alert> dbAlerts) {
		for (Alert dbAlert : dbAlerts) {
			category.addAlert(convertToAlertWithDomain(dbAlert));
		}
	}

}
