package com.dianping.cat.report.page.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private LocationData m_data;

	private String m_cb;
	
	public String getCb() {
   	return m_cb;
   }

	public void setCb(String cb) {
   	m_cb = cb;
   }

	public LocationData getData() {
		return m_data;
	}

	public void setData(LocationData data) {
		m_data = data;
	}

	public String getLocationDetail() {
		return m_data.getJsonString();
	}

	public HeatMapReport getReport() {
		return new HeatMapReport();
	}

	public Date getCreatTime() {
		return new Date();
	}

	public Model(Context ctx) {
		super(ctx);
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
}
