package com.dianping.cat.report.task.alert.summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.system.tool.MailSMS;
import com.site.helper.Splitters;

public class AlertSummaryExecutor {

	@Inject
	AlertSummaryGenerator m_alertSummaryGenerator;

	@Inject
	AlertSummaryManager m_alertSummaryManager;

	@Inject
	AlertSummaryDecorator m_alertSummaryDecorator;

	@Inject
	protected MailSMS m_mailSms;

	private String buildMailTitle(String domain, Date date) {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		builder.append("[统一告警] [项目 ").append(domain).append("]");
		builder.append("[时间 ").append(dateFormat.format(date)).append("]");
		return builder.toString();
	}

	private List<String> builderReceivers(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
	}

	public String execute(String domain, Date date, String receiverStr) {
		try {
			AlertSummary alertSummary = m_alertSummaryGenerator.generateAlertSummary(domain, date);
			m_alertSummaryManager.insert(alertSummary);
			String title = buildMailTitle(domain, date);
			String content = m_alertSummaryDecorator.generateHtml(alertSummary);
			List<String> receivers = builderReceivers(receiverStr);
			m_mailSms.sendEmail(title, content, receivers);

			return content;
		} catch (Exception e) {
			Cat.logError("generate alert summary fail:" + domain + " " + date, e);
			return null;
		}
	}

}
