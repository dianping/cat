package com.dianping.cat.report.task.alert.summary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.site.helper.Splitters;
import com.site.lookup.util.StringUtils;

public class AlertSummaryExecutor {

	@Inject(type = SummaryContentGenerator.class, value = AlertSummaryContentGenerator.ID)
	private SummaryContentGenerator m_alertSummaryContentGenerator;

	@Inject(type = SummaryContentGenerator.class, value = FailureSummaryContentGenerator.ID)
	private SummaryContentGenerator m_failureSummaryContentGenerator;

	@Inject(type = SummaryContentGenerator.class, value = AlterationSummaryContentGenerator.ID)
	private SummaryContentGenerator m_alterationSummaryContentGenerator;

	@Inject
	private SenderManager m_sendManager;

	public static final long SUMMARY_DURATION = 5 * 60 * 1000L;

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
			StringBuilder builder = new StringBuilder();

			String summaryContent = m_alertSummaryContentGenerator.generateHtml(domain, date);
			builder.append(summaryContent);

			String failureContext = m_failureSummaryContentGenerator.generateHtml(domain, date);
			builder.append(failureContext);

			String alterationContext = m_alterationSummaryContentGenerator.generateHtml(domain, date);
			builder.append(alterationContext);

			t.setStatus(Transaction.SUCCESS);
			return builder.toString();
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
