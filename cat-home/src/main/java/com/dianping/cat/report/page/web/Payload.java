package com.dianping.cat.report.page.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.tuple.Pair;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("city")
	private String m_city = "上海市";

	@FieldMeta("channel")
	private String m_channel = "";

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("type")
	private String m_type = Constants.TYPE_INFO;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.WEB);
	}

	private Date generateDate(String time, long start) {
		Date date = null;
		String[] times = time.split(":");

		if (times.length == 2) {
			int hour = Integer.parseInt(times[0]);
			int minute = Integer.parseInt(times[1]);
			if (minute > 0) {
				hour += 1;
			}

			date = new Date(TimeHelper.getCurrentDay(start).getTime() + hour * TimeHelper.ONE_HOUR);
			if (date.equals(TimeHelper.getCurrentDay(start, 1))) {
				date = new Date(date.getTime() - TimeHelper.ONE_MINUTE);
			}
		} else {
			date = TimeHelper.getCurrentHour(1);
		}
		return date;
	}

	private Date generateDefaultEnd() {
		Date date = TimeHelper.getCurrentHour(1);
		if (date.equals(TimeHelper.getCurrentDay(System.currentTimeMillis(), 1))) {
			date = new Date(date.getTime() - TimeHelper.ONE_MINUTE);
		}
		return date;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getChannel() {
		return m_channel;
	}

	public String getCity() {
		return m_city;
	}

	public String getGroup() {
		return m_group;
	}

	public Pair<Date, Date> getHistoryEndDatePair() {
		Date currentEnd = generateDefaultEnd();
		Date compareEnd = null;

		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				String[] ends = m_customEnd.split(";");
				Pair<Date, Date> startDatePair = getHistoryStartDatePair();
				long start = startDatePair.getKey().getTime();
				currentEnd = generateDate(ends[0], start);

				if (ends.length == 2) {
					start = startDatePair.getValue().getTime();
					compareEnd = generateDate(ends[1], start);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<Date, Date>(currentEnd, compareEnd);
	}

	public Pair<Date, Date> getHistoryStartDatePair() {
		Date currentStart = TimeHelper.getCurrentDay();
		Date compareStart = null;

		try {
			if (m_customStart != null && m_customStart.length() > 0) {
				String[] starts = m_customStart.split(";");
				Date current = m_format.parse(starts[0]);
				currentStart = new Date(current.getTime() - current.getTime() % TimeHelper.ONE_HOUR);

				if (starts.length == 2) {
					Date compare = m_format.parse(starts[1]);
					compareStart = new Date(compare.getTime() - compare.getTime() % TimeHelper.ONE_HOUR);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<Date, Date>(currentStart, compareStart);
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getType() {
		return m_type;
	}

	public String getUrl() {
		return m_url;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.WEB);
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
