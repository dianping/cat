package com.dianping.cat.report.page.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private DisplayMatrix m_matrix;

	private MatrixReport m_report;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	@Override
	public String getDomain() {
		return m_report.getDomain();
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public DisplayMatrix getMatrix() {
		return m_matrix;
	}

	public MatrixReport getReport() {
		return m_report;
	}

	public void setMatrix(DisplayMatrix matrix) {
		m_matrix = matrix;
	}

	public void setReport(MatrixReport report) {
		m_report = report;
	}
}
