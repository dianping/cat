package com.dianping.cat.report.page.test;

import java.util.List;

import com.dianping.cat.home.dal.report.Test;
import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	
	private String m_name;
	private Test m_testList;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public Test getTestList() {
		return m_testList;
	}

	public void setTestList(Test testList) {
		m_testList = testList;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public Action getDefaultAction() {
		return Action.INSERT;
	}

	@Override
   public String toString() {
	   return "Model [m_testList=" + m_testList.toString() + "]";
   }
	
	
}
