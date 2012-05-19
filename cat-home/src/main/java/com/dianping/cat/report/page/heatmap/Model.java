package com.dianping.cat.report.page.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private String m_cb;

	private String m_locationData;

	private int m_max;
	
	private HeatMapReport m_report;
	
	private String m_display;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public String getCb() {
		return m_cb;
	}

	public Date getCreatTime() {
		return new Date();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return "MobileApi";
	}

	@Override
	public Collection<String> getDomains() {
		Collection<String> domains = new ArrayList<String>();
		domains.add("MobileApi");
		return domains;
	}

	public String getLocationData() {
		return m_locationData;
	}

	public int getMax() {
   	return m_max;
   }

	public HeatMapReport getReport() {
   	return m_report;
   }

	public void setReport(HeatMapReport report) {
   	m_report = report;
   }

	public void setCb(String cb) {
		m_cb = cb;
	}

	public void setLocationData(String locationData) {
		m_locationData = locationData;
	}

	public String getDisplay() {
   	return m_display;
   }

	public void setDisplay(String display) {
   	m_display = display;
   }

	public void setMax(int max) {
   	m_max = max;
   }
}
