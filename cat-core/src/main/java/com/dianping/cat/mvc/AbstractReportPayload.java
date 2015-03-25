package com.dianping.cat.mvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.ModelPeriod;

public abstract class AbstractReportPayload<A extends Action, P extends Page> implements ActionPayload<P, A> {

	@FieldMeta("endDate")
	protected String m_customEnd;

	@FieldMeta("startDate")
	protected String m_customStart;

	@FieldMeta("date")
	protected long m_date;

	@FieldMeta("domain")
	private String m_domain = Constants.CAT;

	@FieldMeta("ip")
	private String m_ipAddress = Constants.ALL;

	protected P m_page;

	@FieldMeta("reportType")
	private String m_reportType = "day";

	@FieldMeta("step")
	protected int m_step;

	private SimpleDateFormat m_hourlyFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	protected P m_defaultPage;

	public AbstractReportPayload(P defaultPage) {
		m_defaultPage = defaultPage;
	}

	private void checkFutureDate() {
		if ("day".equals(m_reportType)) {
			Calendar today = Calendar.getInstance();
			long current = getCurrentDate();

			today.setTimeInMillis(current);
			today.set(Calendar.HOUR_OF_DAY, 0);
			if (m_date == today.getTimeInMillis()) {
				m_date = m_date - 24 * TimeHelper.ONE_HOUR;
			}
		}
	}

	public void computeHistoryDate() {
		if (m_date <= 0) {
			m_date = TimeHelper.getCurrentDay(-1).getTime();
		}
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(m_date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		m_date = cal.getTimeInMillis();

		if ("month".equals(m_reportType)) {
			cal.set(Calendar.DATE, 1);
			m_date = cal.getTimeInMillis();
		} else if ("week".equals(m_reportType)) {
			int weekOfDay = cal.get(Calendar.DAY_OF_WEEK) % 7;
			m_date = m_date - (TimeHelper.ONE_HOUR) * (weekOfDay % 7) * 24;
			if (m_date > System.currentTimeMillis()) {
				m_date = m_date - 7 * 24 * TimeHelper.ONE_HOUR;
			}
			cal.setTimeInMillis(m_date);
		}

		if (m_step < 0) {
			if ("month".equals(m_reportType)) {
				cal.add(Calendar.MONTH, m_step);
				m_date = cal.getTimeInMillis();
			} else if ("week".equals(m_reportType)) {
				m_date = m_date + 7 * (TimeHelper.ONE_HOUR * 24) * m_step;
			} else if ("day".equals(m_reportType)) {
				m_date = m_date + (TimeHelper.ONE_HOUR * 24) * m_step;
			}
		} else {
			long temp = 0;
			if ("month".equals(m_reportType)) {
				cal.add(Calendar.MONTH, m_step);
				temp = cal.getTimeInMillis();
			} else if ("week".equals(m_reportType)) {
				temp = m_date + 7 * (TimeHelper.ONE_HOUR * 24) * m_step;
			} else if ("day".equals(m_reportType)) {
				temp = m_date + (TimeHelper.ONE_HOUR * 24) * m_step;
			}
			if (temp <= getCurrentStartDay()) {
				m_date = temp;
			}
		}

		checkFutureDate();
	}

	public long getCurrentDate() {
		long timestamp = System.currentTimeMillis();

		return timestamp - timestamp % TimeHelper.ONE_HOUR;
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
		long extra = m_step * TimeHelper.ONE_HOUR;

		if (m_date <= 0) {
			return current + extra;
		} else {
			long result = m_date + extra;

			if (result > current) {
				return current;
			} else {
				return result;
			}
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
				if (m_customEnd.length() == 8) {
					return m_dayFormat.parse(m_customEnd);
				} else if (m_customEnd.length() == 10) {
					return m_hourlyFormat.parse(m_customEnd);
				}
			} catch (ParseException e) {
			}
		}

		long temp = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		if ("month".equals(m_reportType)) {
			int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			temp = m_date + maxDay * (TimeHelper.ONE_HOUR * 24);
		} else if ("week".equals(m_reportType)) {
			temp = m_date + 7 * (TimeHelper.ONE_HOUR * 24);
		} else {
			temp = m_date + (TimeHelper.ONE_HOUR * 24);
		}
		cal.setTimeInMillis(temp);
		return cal.getTime();
	}

	public Date getHistoryStartDate() {
		if (m_customStart != null) {
			try {
				if (m_customStart.length() == 8) {
					return m_dayFormat.parse(m_customStart);
				} else if (m_customStart.length() == 10) {
					return m_hourlyFormat.parse(m_customStart);
				}
			} catch (ParseException e) {
			}
		}
		return new Date(m_date);
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	@Override
	public P getPage() {
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
				if (date.length() == 10) {
					temp = m_hourlyFormat.parse(date);
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
		if (StringUtils.isNotEmpty(domain)) {
			m_domain = domain;
		}
	}

	public void setIpAddress(String ipAddress) {
		if (StringUtils.isNotEmpty(ipAddress)) {
			m_ipAddress = ipAddress;
		}
	}

	public void setPage(P page) {
		m_page = page;
	}

	public void setReportType(String reportType) {
		if (StringUtils.isNotEmpty(reportType)) {
			m_reportType = reportType;
		}
	}

	public void setStep(int nav) {
		m_step = nav;
	}

}
