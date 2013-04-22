package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.HashSet;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private MetricReport m_report;
	
	private MetricDisplay m_display;

	private String m_domain;
	
	private String m_group;
	
	private String m_channel;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public MetricDisplay getDisplay() {
		return m_display;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public MetricReport getReport() {
		return m_report;
	}

	public void setDisplay(MetricDisplay display) {
		m_display = display;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setReport(MetricReport report) {
		m_report = report;
	}

	public String getGroup() {
		return m_group;
	}

	public String getChannel() {
		return m_channel;
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public void setGroup(String group) {
		m_group = group;
	}
	
}
