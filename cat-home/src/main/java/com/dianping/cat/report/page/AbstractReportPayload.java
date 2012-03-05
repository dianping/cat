package com.dianping.cat.report.page;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.site.web.mvc.Action;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public abstract class AbstractReportPayload<A extends Action> implements ActionPayload<ReportPage, A> {
	protected static final long ONE_HOUR = 3600 * 1000L;

	private ReportPage m_defaultPage;

	private ReportPage m_page;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("date")
	private long m_date;

	@FieldMeta("hours")
	private int m_hours;

	public AbstractReportPayload(ReportPage defaultPage) {
		m_defaultPage = defaultPage;
	}

	public long getCurrentDate() {
		long timestamp = System.currentTimeMillis();

		return timestamp - timestamp % ONE_HOUR;
	}

	public long getDate() {
		if (m_date <= 0) {
			long current = getCurrentDate();

			return current + m_hours * ONE_HOUR;
		} else {
			return m_date + m_hours * ONE_HOUR;
		}
	}

	public String getDomain() {
		return m_domain;
	}

	public int getHours() {
		return m_hours;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ModelPeriod getPeriod() {
		long current = getCurrentDate();
		long date = getDate();

		if (date >= current + ONE_HOUR) {
			return ModelPeriod.FUTURE;
		} else if (date >= current) {
			return ModelPeriod.CURRENT;
		} else if (date >= current - ONE_HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
	}

	public void setDate(long date) {
		m_date = date;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setHours(int hours) {
		m_hours = hours;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, m_defaultPage);
	}
}
