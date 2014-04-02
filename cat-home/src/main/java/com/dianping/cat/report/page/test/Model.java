package com.dianping.cat.report.page.test;

import java.util.List;

import com.dianping.cat.home.dal.report.Family;
import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_insertResult;
	private List<Family> m_families;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.QUERYALL;
	}

	public List<Family> getFamilies() {
		return m_families;
	}

	public String getInsertResult() {
		return m_insertResult;
	}

	public void setFamilies(List<Family> families) {
		m_families = families;
	}

	public void setInsertResult(String insertResult) {
		m_insertResult = insertResult;
	}
}
