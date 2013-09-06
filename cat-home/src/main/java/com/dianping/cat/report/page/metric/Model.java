package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.LineChart;

public class Model extends AbstractReportModel<Action, Context> {

	private List<LineChart> m_lineCharts;

	private Collection<ProductLine> m_productLines;
	
	private Date m_startTime;
	
	private Date m_endTime;

	private Map<Integer, Abtest> m_abtests;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.METRIC;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public Collection<ProductLine> getProductLines() {
   	return m_productLines;
   }

	public void setProductLines(Collection<ProductLine> productLines) {
   	m_productLines = productLines;
   }

	public List<LineChart> getLineCharts() {
   	return m_lineCharts;
   }

	public void setLineCharts(List<LineChart> lineCharts) {
   	m_lineCharts = lineCharts;
   }

	public Date getStartTime() {
   	return m_startTime;
   }

	public void setStartTime(Date startTime) {
   	m_startTime = startTime;
   }

	public Date getEndTime() {
   	return m_endTime;
   }

	public void setEndTime(Date endTime) {
   	m_endTime = endTime;
   }

	public void setAbtests(Map<Integer, Abtest> abtests) {
		m_abtests = abtests;
   }

	public Map<Integer, Abtest> getAbtests() {
   	return m_abtests;
   }
	
}
