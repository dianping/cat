package com.dianping.cat.report.page.dependency;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("minute")
	private String m_minute;
	
	@FieldMeta("all")
	private boolean m_all;
	
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("productLine")
	private String productLine;

	public Payload() {
		super(ReportPage.DEPENDENCY);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getMinute() {
   	return m_minute;
   }

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LINE_CHART);
	}

	public void setMinute(String minute) {
   	this.m_minute = minute;
   }

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DEPENDENCY);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LINE_CHART;
		}
	}

	public boolean isAll() {
   	return m_all;
   }

	public void setAll(boolean all) {
   	this.m_all = all;
   }

	public String getProductLine() {
   	return productLine;
   }

	public void setProductLine(String productLine) {
   	this.productLine = productLine;
   }
	
}
