package com.dianping.cat.report.page.top;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("count")
	private int m_minuteCounts = 10;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("tops")
	private int m_topCounts = 10;

	@FieldMeta("refresh")
	private boolean m_refresh;

	public int getFrequency() {
   	return m_frequency;
   }

	public void setFrequency(int frequency) {
   	m_frequency = frequency;
   }

	public Payload() {
		super(ReportPage.TOP);
	}

	public boolean getRefresh() {
		return m_refresh;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public int getMinuteCounts() {
   	return m_minuteCounts;
   }

	public void setMinuteCounts(int minuteCount) {
   	m_minuteCounts = minuteCount;
   }

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TOP);
	}

	public int getTopCounts() {
		return m_topCounts;
	}

	public void setTopCounts(int tops) {
		m_topCounts = tops;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
