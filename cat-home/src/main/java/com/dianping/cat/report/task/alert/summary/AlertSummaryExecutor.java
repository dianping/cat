package com.dianping.cat.report.task.alert.summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.site.helper.Splitters;
import com.site.lookup.util.StringUtils;

public class AlertSummaryExecutor {

	@Inject
	private AlertSummaryGenerator m_alertSummaryGenerator;

	@Inject
	private AlertSummaryManager m_alertSummaryManager;

	@Inject(type = AlertSummaryDecorator.class, value = AlertSummaryFTLDecorator.ID)
	private AlertSummaryDecorator m_alertSummaryDecorator;

	@Inject
	private SenderManager m_sendManager;

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
		if (StringUtils.isEmpty(domain) || date == null) {
			return null;
		}

		try {
			AlertSummary alertSummary = m_alertSummaryGenerator.generateAlertSummary(domain, date);
			m_alertSummaryManager.insert(alertSummary);
			String title = buildMailTitle(domain, date);
			String content = m_alertSummaryDecorator.generateHtml(alertSummary);
			List<String> receivers = builderReceivers(receiverStr);

			AlertMessageEntity message = new AlertMessageEntity(domain, title, content, receivers);

			m_sendManager.sendAlert(AlertChannel.MAIL, "AlertSummary", message);
			return content;
		} catch (Exception e) {
			Cat.logError("generate alert summary fail:" + domain + " " + date, e);
			return null;
		}
	}

}
