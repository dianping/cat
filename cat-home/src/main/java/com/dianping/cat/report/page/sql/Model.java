package com.dianping.cat.report.page.sql;

import java.util.ArrayList;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta(SqlAnalyzer.ID)
public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	private SqlReport m_report;

	@EntityMeta
	private DisplaySqlReport m_displaySqlReport;

	private String m_database;

	private String m_pieChart;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDatabase() {
		return m_database;
	}

	public List<String> getDatabases() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDatabaseNames());
		}
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	public DisplaySqlReport getDisplaySqlReport() {
		return m_displaySqlReport;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
	}

	public SqlReport getReport() {
		return m_report;
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public void setDisplaySqlReport(DisplaySqlReport displaySqlReport) {
		m_displaySqlReport = displaySqlReport;
	}

	public void setReport(SqlReport sqlReport) {
		m_report = sqlReport;
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

}
