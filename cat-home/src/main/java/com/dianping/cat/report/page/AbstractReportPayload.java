package com.dianping.cat.report.page;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.model.ModelPeriod;

public abstract class AbstractReportPayload<A extends Action> implements ActionPayload<ReportPage, A> {

	@FieldMeta("endDate")
	private String m_customEnd;

	@FieldMeta("startDate")
	private String m_customStart;

	@FieldMeta("date")
	private long m_date;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ipAddress;

	private ReportPage m_page;

	@FieldMeta("reportType")
	private String m_reportType;

	@FieldMeta("step")
	private int m_step;

	@FieldMeta("today")
	private boolean m_today;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private ReportPage m_defaultPage;

	public AbstractReportPayload(ReportPage defaultPage) {
		m_defaultPage = defaultPage;
	}

	public void computeStartDate() {
		m_date = getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		m_date = cal.getTimeInMillis();

		if ("month".equals(m_reportType)) {
			cal.set(Calendar.DATE, 1);
			m_date = cal.getTimeInMillis();
		} else if ("week".equals(m_reportType)) {
			int weekOfDay = cal.get(Calendar.DAY_OF_WEEK) % 7;
			m_date = m_date - (TimeUtil.ONE_HOUR) * (weekOfDay % 7) * 24;
			if (m_date > System.currentTimeMillis()) {
				m_date = m_date - 7 * 24 * TimeUtil.ONE_HOUR;
			}
			cal.setTimeInMillis(m_date);
		}

		if (m_step < 0) {
			if ("month".equals(m_reportType)) {
				cal.add(Calendar.MONTH, m_step);
				m_date = cal.getTimeInMillis();
			} else if ("week".equals(m_reportType)) {
				m_date = m_date + 7 * (TimeUtil.ONE_HOUR * 24) * m_step;
			} else if ("day".equals(m_reportType)) {
				m_date = m_date + (TimeUtil.ONE_HOUR * 24) * m_step;
			}
		} else {
			long temp = 0;
			if ("month".equals(m_reportType)) {
				cal.add(Calendar.MONTH, m_step);
				temp = cal.getTimeInMillis();
			} else if ("week".equals(m_reportType)) {
				temp = m_date + 7 * (TimeUtil.ONE_HOUR * 24) * m_step;
			} else if ("day".equals(m_reportType)) {
				temp = m_date + (TimeUtil.ONE_HOUR * 24) * m_step;
			}
			if (temp <= getCurrentStartDay()) {
				m_date = temp;
			}
		}
	}

	public long getCurrentDate() {
		long timestamp = System.currentTimeMillis();

		return timestamp - timestamp % TimeUtil.ONE_HOUR;
	}

	public long getCurrentStartDay() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();

		cal.setTime(new Date(timestamp));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		return cal.getTimeInMillis();
	}

	public long getDate() {
		long current = getCurrentDate();

		long extra = m_step * TimeUtil.ONE_HOUR;
		if (m_reportType != null
		      && (m_reportType.equals("day") || m_reportType.equals("month") || m_reportType.equals("week"))) {
			extra = 0;
		}
		if (m_date <= 0) {
			return current + extra;
		} else {
			long result = m_date + extra;

			if (result > current) {
				return current;
			}
			return result;
		}
	}

	public String getDomain() {
		return m_domain;
	}

	public Date getHistoryDisplayEndDate() {
		Date date = getHistoryEndDate();
		return new Date(date.getTime() - 1000);
	}

	public Date getHistoryEndDate() {
		if (m_customEnd != null) {
			try {
				return m_dayFormat.parse(m_customEnd);
			} catch (ParseException e) {
			}
		}

		long temp = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		if ("month".equals(m_reportType)) {
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			temp = m_date + maxDay * (TimeUtil.ONE_HOUR * 24);
		} else if ("week".equals(m_reportType)) {
			temp = m_date + 7 * (TimeUtil.ONE_HOUR * 24);
		} else {
			temp = m_date + (TimeUtil.ONE_HOUR * 24);
		}
		cal.setTimeInMillis(temp);
		return cal.getTime();
	}

	public Date getHistoryStartDate() {
		if (m_customStart != null) {
			try {
				return m_dayFormat.parse(m_customStart);
			} catch (ParseException e) {
			}
		}
		return new Date(m_date);
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ModelPeriod getPeriod() {
		return ModelPeriod.getByTime(getDate());
	}

	public long getRealDate() {
		return m_date;
	}

	public String getReportType() {
		return m_reportType;
	}

	public int getStep() {
		return m_step;
	}

	public boolean isToday() {
		return m_today;
	}

	public void setCustomEnd(String customEnd) {
		m_customEnd = customEnd;
	}

	public void setCustomStart(String customStart) {
		m_customStart = customStart;
	}

	public void setDate(String date) {
		if (date == null || date.length() == 0) {
			m_date = getCurrentDate();
		} else {
			try {
				Date temp = null;
				if (date != null && date.length() == 10) {
					temp = m_dateFormat.parse(date);
				} else {
					temp = m_dayFormat.parse(date);
				}
				m_date = temp.getTime();
			} catch (Exception e) {
				// ignore it
				m_date = getCurrentDate();
			}
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, m_defaultPage);
	}

	public void setReportType(String reportType) {
		this.m_reportType = reportType;
	}

	public void setStep(int nav) {
		m_step = nav;
	}

	public void setToday(boolean today) {
		m_today = today;
	}

	// yestoday is default
	public void setYesterdayDefault() {
		if ("day".equals(m_reportType)) {
			Calendar today = Calendar.getInstance();
			long current = getCurrentDate();

			today.setTimeInMillis(current);
			today.set(Calendar.HOUR_OF_DAY, 0);
			if (m_date == today.getTimeInMillis()) {
				m_date = m_date - 24 * TimeUtil.ONE_HOUR;
			}
		}
	}

}
