package com.dianping.cat.report.page.applog.display;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dianping.cat.configuration.mobile.entity.Item;
import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.service.FieldsInfo;

public class AppLogDisplayInfo {

	private Collection<Item> m_appNames;

	private List<LogMsg> m_msgs;

	private FieldsInfo m_fieldsInfo;

	private Map<String, PieChart> m_msgDistributions;

	public List<LogMsg> getMsgs() {
		return m_msgs;
	}

	public void setMsgs(List<LogMsg> msgs) {
		m_msgs = msgs;
	}

	public FieldsInfo getFieldsInfo() {
		return m_fieldsInfo;
	}

	public void setFieldsInfo(FieldsInfo fieldsInfo) {
		m_fieldsInfo = fieldsInfo;
	}

	public Collection<Item> getAppNames() {
		return m_appNames;
	}

	public void setAppNames(Collection<Item> appNames) {
		m_appNames = appNames;
	}

	public Map<String, PieChart> getMsgDistributions() {
		return m_msgDistributions;
	}

	public void setMsgDistributions(Map<String, PieChart> msgDistributions) {
		m_msgDistributions = msgDistributions;
	}

}
