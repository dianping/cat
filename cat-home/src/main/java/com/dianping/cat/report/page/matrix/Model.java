package com.dianping.cat.report.page.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

@ModelMeta(MatrixAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	@EntityMeta
	private DisplayMatrix m_matrix;

	@EntityMeta
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

			return SortHelper.sortDomain(domainNames);
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
