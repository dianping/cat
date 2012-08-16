package com.dianping.cat.notify.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.notify.util.TimeUtil;

public class DailyReportCreater extends AbstractReportCreater {

	private static final String PATTERN = "<a href='http://cat.dianpingoa.com/cat/r/%s?op=history&domain=%s&date=%s&reportType=day' target='_blank'>( %s %s)</a>";

	public boolean isNeedToCreate(long timestamp) {
		int hour = TimeUtil.getHourOfDay(timestamp);
		/* create report at 01:00:00 */
		if (hour != 1) {
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
	protected String renderTransactionReport(TimeSpan timeSpan, TransactionReport transactionReport, String domain) {
		if(null == transactionReport){
			return "";
		}
		List<TransactionRenderDO> tRenderDoList = getTransactionRenderDoList(timeSpan, transactionReport, domain, false);
		if (tRenderDoList == null || tRenderDoList.size() == 0) {
			return "";
		}

		long period = timeSpan.getTimeStamp();
		Map<String, Object> params = new HashMap<String, Object>();

		String currentUrl = getCurrentViewUrl("t", domain, period);
		params.put("title", "Transaction Report " + currentUrl);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 7;
		params.put("preWeakLastDay", getViewUrl("t", domain, preWeakLastDay));
		params.put("preWeakDay", getViewUrl("t", domain, preWeakDay));
		params.put("typeList", tRenderDoList);

		String templatePath = m_config.getTemplates().get("transaction").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renderEventReport(TimeSpan timeSpan, EventReport report, String domain) {
		if(null == report){
			return "";
		}
		List<EventRenderDO> eRenderDoList = getEventRenderDoList(timeSpan, report, domain, false);
		if (eRenderDoList == null || eRenderDoList.size() == 0) {
			return "";
		}

		long period = timeSpan.getTimeStamp();
		Map<String, Object> params = new HashMap<String, Object>(2);

		String currentUrl = getCurrentViewUrl("e", domain, period);
		params.put("title", "Event Report " + currentUrl);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 7;
		params.put("preWeakLastDay", getViewUrl("e", domain, preWeakLastDay));
		params.put("preWeakDay", getViewUrl("e", domain, preWeakDay));

		params.put("typeList", eRenderDoList);
		String templatePath = m_config.getTemplates().get("event").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renterProblemReport(TimeSpan timeSpan, ProblemReport report, String domain) {
		if(null == report){
			return "";
		}
		List<ProblemRenderDO> pRenderDoList = getProblemRenderDoList(timeSpan, report, domain, false);
		if (pRenderDoList == null || pRenderDoList.size() == 0) {
			return "";
		}

		Map<String, Object> params = new HashMap<String, Object>(2);

		long period = timeSpan.getTimeStamp();
		String currentUrl = getCurrentViewUrl("p", domain, period);
		params.put("title", "Problem Report " + currentUrl);

		long preWeakLastDay = period - TimeUtil.DAY_MICROS;
		long preWeakDay = period - TimeUtil.DAY_MICROS * 7;
		params.put("preWeakLastDay", getViewUrl("p", domain, preWeakLastDay));
		params.put("preWeakDay", getViewUrl("p", domain, preWeakDay));

		params.put("typeList", pRenderDoList);
		String templatePath = m_config.getTemplates().get("problem").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	private String getViewUrl(String reportType, String domain, long timestamp) {
		String weakname = TimeUtil.getDayNameOfWeak(timestamp);
		String date = TimeUtil.formatTime("yyyy-MM-dd", timestamp);
		return String.format(PATTERN, reportType, domain, TimeUtil.formatTime("yyyyMMdd", timestamp), weakname, date);
	}

}
