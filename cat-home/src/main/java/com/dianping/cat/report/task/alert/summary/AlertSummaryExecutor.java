package com.dianping.cat.report.task.alert.summary;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;

public class AlertSummaryExecutor {

	@Inject
	AlertSummaryGenerator m_alertSummaryGenerator;

	@Inject
	AlertSummaryManager m_alertSummaryManager;

	@Inject
	AlertSummaryDecorator m_alertSummaryDecorator;

	@Inject
	AlertSummarySender m_alertSummarySender;

	public String execute(String domain, Date date, List<String> receivers) {
		try {
			AlertSummary alertSummary = m_alertSummaryGenerator.generateAlertSummary(domain, date);
			m_alertSummaryManager.insert(alertSummary);
			String content = m_alertSummaryDecorator.generateHtml(alertSummary);
			m_alertSummarySender.send(content, receivers);

			return content;
		} catch (Exception e) {
			Cat.logError("generate alert summary fail:" + domain + " " + date, e);
			return null;
		}
	}

}
