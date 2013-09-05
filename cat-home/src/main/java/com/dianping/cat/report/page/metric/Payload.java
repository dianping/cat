package com.dianping.cat.report.page.metric;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("product")
	private String m_product;

	@FieldMeta("test")
	private String m_test = "-1";
	
	@FieldMeta("timeRange")
	private int m_timeRange = 2;

	public Payload() {
		super(ReportPage.METRIC);
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
		m_action = Action.getByName(action, Action.METRIC);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.METRIC);
	}

	public String getProduct() {
		return m_product;
	}

	public void setProduct(String product) {
		m_product = product;
	}

	public String getTest() {
		return m_test;
	}

	public void setTest(String test) {
		m_test = test;
	}
	
	public int getTimeRange() {
		return m_timeRange;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.METRIC;
		}
	}
	
	
}
