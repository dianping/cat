package com.dianping.cat.notify.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.notify.job.ProblemStatistics;
import com.dianping.cat.notify.util.TimeUtil;

public class DailyReportCreater extends AbstractReportCreater {

	private static final String PATTERN = "<a href='http://cat.dianpingoa.com/cat/r/%s?op=history&domain=%s&date=%s&reportType=day'>(Last %s %s)</a>";

	public boolean isNeedToCreate(long timestamp) {
		
		int hour = TimeUtil.getHourOfDay(timestamp);
		/* create report at 00:00:00 */
		if (hour != 0) {
			return false;
		}
		long currentTime = System.currentTimeMillis();
		if (lastSuccessTime.get() == -1) {
			/* for first time */
			lastSuccessTime.set(currentTime);
			return true;
		}

		long timespan = currentTime - lastSuccessTime.get();
		if (timespan > TimeUtil.HOUR_MICROS) {
			/* next time */
			lastSuccessTime.set(currentTime);
			return true;
		}
		return false;
	}

	@Override
	protected TimeSpan getReportTimeSpan(long timespan) {
		long startMicros = timespan - TimeUtil.TWO_DAY_MICROS;
		long endMicros = timespan - TimeUtil.DAY_MICROS;
		TimeSpan timeRange = new TimeSpan();
		timeRange.setStartMicros(startMicros);
		timeRange.setEndMicros(endMicros);
		timeRange.setTimeStamp(timespan);
		return timeRange;
	}

	@Override
	protected String renderTransactionReport(TimeSpan timeSpan,
			TransactionReport transactionReport) {
		com.dianping.cat.consumer.transaction.model.entity.Machine machine = transactionReport
				.findMachine(ReportConstants.ALL_IP);
		if (machine == null) {
			return null;
		}
		List<TransactionType> typeList = new ArrayList<TransactionType>(machine
				.getTypes().values());
		Collections.sort(typeList, new Comparator<TransactionType>() {
			@Override
			public int compare(TransactionType o1, TransactionType o2) {
				return (int) (o1.getAvg() - o2.getAvg());
			}
		});
		long period = transactionReport.getStartTime().getTime();
		Map<String, Object> params = new HashMap<String, Object>();
		String reportDay = TimeUtil.formatTime("yyyy-MM-dd", period);
		params.put("title", "Transaction Report " + reportDay);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS * 7;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 6;
		params.put("preWeakLastDay",
				getViewUrl("t", getDomain(), preWeakLastDay));
		params.put("preWeakDay", getViewUrl("t", getDomain(), preWeakDay));

		params.put("typeList", typeList);
		String templatePath = m_config.getTemplates().get("transaction")
				.getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renderEventReport(TimeSpan timeSpan, EventReport report) {
		com.dianping.cat.consumer.event.model.entity.Machine machine = report
				.findMachine(ReportConstants.ALL_IP);
		if (machine == null) {
			return null;
		}
		List<EventType> eventTypeList = new ArrayList<EventType>(machine
				.getTypes().values());
		Collections.sort(eventTypeList, new Comparator<EventType>() {
			@Override
			public int compare(EventType o1, EventType o2) {
				return (int) (o1.getTotalCount() - o2.getTotalCount());
			}
		});

		long period = report.getStartTime().getTime();
		Map<String, Object> params = new HashMap<String, Object>(2);
		String reportDay = TimeUtil.formatTime("yyyy-MM-dd", period);
		params.put("title", "Event Report " + reportDay);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS * 7;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 6;
		params.put("preWeakLastDay",
				getViewUrl("e", getDomain(), preWeakLastDay));
		params.put("preWeakDay", getViewUrl("e", getDomain(), preWeakDay));

		params.put("typeList", eventTypeList);
		String templatePath = m_config.getTemplates().get("event").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renterProblemReport(TimeSpan timeSpan, ProblemReport report) {
		ProblemStatistics problemStatistics = new ProblemStatistics();
		problemStatistics.setAllIp(true);
		problemStatistics.visitProblemReport(report);
		Map<String, Object> params = new HashMap<String, Object>(2);

		long period = report.getStartTime().getTime();
		String reportDay = TimeUtil.formatTime("yyyy-MM-dd", period);
		params.put("title", "Problem Report " + reportDay);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS * 7;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 6;
		params.put("preWeakLastDay",
				getViewUrl("p", getDomain(), preWeakLastDay));
		params.put("preWeakDay", getViewUrl("p", getDomain(), preWeakDay));

		params.put("problemStatistics", problemStatistics);
		String templatePath = m_config.getTemplates().get("problem").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	private String getViewUrl(String reportType, String domain, long timestamp) {
		String weakname = TimeUtil.getDayNameOfWeak(timestamp);
		String date = TimeUtil.formatTime("yyyy-MM-dd", timestamp);
		return String.format(PATTERN, reportType, domain,
				TimeUtil.formatTime("yyyyMMdd", timestamp), weakname, date);
	}

}
