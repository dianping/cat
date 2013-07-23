package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.HashSet;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private MetricReport m_report;

	private MetricDisplay m_display;

	private Collection<ProductLine> m_productLines;

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
		return getDisplayDomain();
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

	public Collection<ProductLine> getProductLines() {
   	return m_productLines;
   }

	public void setProductLines(Collection<ProductLine> productLines) {
   	m_productLines = productLines;
   }

	public void setReport(MetricReport report) {
		m_report = report;
	}

}
