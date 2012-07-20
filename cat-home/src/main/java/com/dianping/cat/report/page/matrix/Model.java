package com.dianping.cat.report.page.matrix;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private MatrixReport m_report;

	private DisplayMatrix m_matrix;

	public MatrixReport getReport() {
		return m_report;
	}

	public void setReport(MatrixReport report) {
		m_report = report;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	public void setMatrix(DisplayMatrix matrix) {
   	m_matrix = matrix;
   }

	public DisplayMatrix getMatrix() {
		return m_matrix;
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
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
	}
}
