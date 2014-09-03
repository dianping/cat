package com.dianping.cat.report.task.alert.summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.message.Transaction;
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
	private ErrorModelGenerator m_failureContextGenerator;

	@Inject
	private SenderManager m_sendManager;

	@Inject
	private ErrorDecorator m_failureDecorator;

	private List<String> builderReceivers(String str) {
		List<String> result = new ArrayList<String>();

		if (str != null) {
			result.addAll(Splitters.by(",").noEmptyItem().split(str));
		}

		return result;
	}

	private String buildMailTitle(String domain, Date date) {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		builder.append("[统一告警] [项目 ").append(domain).append("]");
		builder.append("[时间 ").append(dateFormat.format(date)).append("]");
		return builder.toString();
	}

	public String execute(String domain, Date date) {
		if (StringUtils.isEmpty(domain) || date == null) {
			return null;
		}
		date = normalizeDate(date);

		Transaction t = Cat.newTransaction("AlertSummary", domain);

		try {
			AlertSummary alertSummary = m_alertSummaryGenerator.generateAlertSummary(domain, date);
			m_alertSummaryManager.insert(alertSummary);
			String summaryContent = m_alertSummaryDecorator.generateHtml(alertSummary);

			Map<Object, Object> failureModel = m_failureContextGenerator.generateFailureModel(domain, date);
			String failureContext = m_failureDecorator.generateHtml(failureModel);

			t.setStatus(Transaction.SUCCESS);
			return summaryContent + failureContext;
		} catch (Exception e) {
			t.setStatus(e);
			Cat.logError("generate alert summary fail:" + domain + " " + date, e);
		} finally {
			t.complete();
		}
		return null;
	}

	public String execute(String domain, Date date, String receiverStr) {
		String content = execute(domain, date);
		if (content == null || "".equals(content)) {
			return null;
		} else {
			String title = buildMailTitle(domain, date);
			List<String> receivers = builderReceivers(receiverStr);
			AlertMessageEntity message = new AlertMessageEntity(domain, title, "alertSummary", content, receivers);

			m_sendManager.sendAlert(AlertChannel.MAIL, message);
		}

		return content;
	}

	private Date normalizeDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

}
