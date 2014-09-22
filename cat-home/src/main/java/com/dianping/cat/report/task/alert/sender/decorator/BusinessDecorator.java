package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.summary.AlertSummaryExecutor;
import com.site.lookup.util.StringUtils;

public class BusinessDecorator extends Decorator {

	@Inject
	private AlertSummaryExecutor m_executor;

	public static final String ID = AlertType.Business.getName();

	@Inject
	protected ProductLineConfigManager m_manager;

	public String buildContactInfo(String domainName) {
		try {
			ProductLine product = m_manager.queryProductLine(domainName);
			String owners = product.getOwner();
			String phones = product.getPhone();
			StringBuilder builder = new StringBuilder();

			if (!StringUtils.isEmpty(owners)) {
				builder.append("[业务负责人: ").append(owners).append(" ]");
			}
			if (!StringUtils.isEmpty(phones)) {
				builder.append("[负责人手机号码: ").append(phones).append(" ]");
			}

			return builder.toString();
		} catch (Exception ex) {
			Cat.logError("build productline contact info error for doamin: " + domainName, ex);
		}

		return "";
	}

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
