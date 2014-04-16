package com.dianping.cat.report.page.test;

import java.util.List;

import com.dianping.cat.home.dal.report.Test;
import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_xml;
	private String m_name;
	private List<Test> m_testList;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public String getXml() {
		return m_xml;
	}

	public void setXml(String xml) {
		this.m_xml = xml;
	}

	public List<Test> getTestList() {
		return m_testList;
	}

	public void setTestList(List<Test> testList) {
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
