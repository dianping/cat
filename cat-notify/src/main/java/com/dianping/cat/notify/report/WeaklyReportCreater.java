package com.dianping.cat.notify.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.notify.util.TimeUtil;

public class WeaklyReportCreater extends AbstractReportCreater {

	public boolean isNeedToCreate(long timestamp) {
		int dayOfWeak = TimeUtil.getDayOfWeak(timestamp);
		int hour = TimeUtil.getHourOfDay(timestamp);
		/* create report at 00:00:00 of every saturday */
		if (dayOfWeak != 7 || hour != 1) {
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
		long startMicros = timespan - TimeUtil.DAY_MICROS * 8;
		long endMicros = timespan - TimeUtil.DAY_MICROS;
		TimeSpan timeRange = new TimeSpan();
		timeRange.setStartMicros(startMicros);
		timeRange.setEndMicros(endMicros);
		timeRange.setTimeStamp(timespan);
		return timeRange;
	}

	@Override
	protected String renderTransactionReport(TimeSpan timeSpan, TransactionReport report, String domain) {
		List<TransactionRenderDO> tRenderDoList = getTransactionRenderDoList(timeSpan, report, domain,true);
		if (tRenderDoList == null || tRenderDoList.size() == 0) {
			return "";
		}
		Map<String, Object> params = new HashMap<String, Object>();

		String startDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getStartMicros());
		String endDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getEndMicros());
		params.put("title", String.format("Weekly Transaction Report [%s to %s]", startDay, endDay));

		params.put("typeList", tRenderDoList);
		String templatePath = m_config.getTemplates().get("transaction").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renderEventReport(TimeSpan timeSpan, EventReport report, String domain) {
		List<EventRenderDO> eRenderDoList = getEventRenderDoList(timeSpan, report, domain,true);
		if (eRenderDoList == null || eRenderDoList.size() == 0) {
			return "";
		}
		Map<String, Object> params = new HashMap<String, Object>(2);

		String startDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getStartMicros());
		String endDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getEndMicros());
		params.put("title", String.format("Weekly Event Report [%s to %s]", startDay, endDay));

		params.put("typeList", eRenderDoList);
		String templatePath = m_config.getTemplates().get("event").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	@Override
	protected String renterProblemReport(TimeSpan timeSpan, ProblemReport report, String domain) {
		List<ProblemRenderDO> pRenderDoList = getProblemRenderDoList(timeSpan, report, domain, true);
		if (pRenderDoList == null || pRenderDoList.size() == 0) {
			return "";
		}

		Map<String, Object> params = new HashMap<String, Object>(2);

		String startDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getStartMicros());
		String endDay = TimeUtil.formatTime("yyyy-MM-dd", timeSpan.getEndMicros());
		params.put("title", String.format("Weekly Problem Report [%s to %s]", startDay, endDay));

		params.put("typeList", pRenderDoList);
		String templatePath = m_config.getTemplates().get("problem").getPath();
		return m_render.fetchAll(templatePath, params);
	}

	
}
