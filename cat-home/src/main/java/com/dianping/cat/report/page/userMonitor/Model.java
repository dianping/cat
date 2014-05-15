package com.dianping.cat.report.page.userMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	
	private List<PatternItem> m_pattermItems;
	
	private List<String> m_cities;
	
	public List<PatternItem> getPattermItems() {
   	return m_pattermItems;
   }

	public void setPattermItems(List<PatternItem> pattermItems) {
   	m_pattermItems = pattermItems;
   }

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
	
	public List<String> getCities() {
   	return m_cities;
   }

	public void setCities(List<String> cities) {
   	m_cities = cities;
   }

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
}
