package com.dianping.cat.report.alert.sender.decorator;

import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;

public class BusinessDecorator extends ProjectDecorator {

	@Inject
	private AlertSummaryExecutor m_executor;

	public static final String ID = AlertType.Business.getName();

	@Inject
	protected ProductLineConfigManager m_manager;

	@Override
	public String generateContent(AlertEntity alert) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(alert.getDate());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date alertDate = cal.getTime();

		StringBuilder sb = new StringBuilder();
		sb.append(alert.getContent());
		sb.append(buildContactInfo(alert.getGroup()));

		String summaryContext = m_executor.execute(alert.getDomain(), alertDate);
		if (summaryContext != null) {
			sb.append("<br/>").append(summaryContext);
		}

		return sb.toString();
	}
	
	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[业务告警] [产品线 ").append(alert.getGroup()).append("]");
		sb.append("[业务指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}
}
