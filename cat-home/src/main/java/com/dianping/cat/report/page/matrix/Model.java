package com.dianping.cat.report.page.matrix;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
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
