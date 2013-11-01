package com.dianping.cat.consumer.sql;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return SqlAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = SqlAnalyzer.ID;

		all.add(C(SqlParseManager.class, ExtendedSqlParseManager.class));
		all.add(C(ReportManager.class, ID, MockSqlReportManager.class).req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedSqlDelegate.class));

		return all;
	}

	public static class ExtendedSqlDelegate extends SqlDelegate {
	}

	public static class MockSqlReportManager extends MockReportManager<SqlReport> {
		private SqlReport m_report;

		@Inject
		private ReportDelegate<SqlReport> m_delegate;

		@Override
		public SqlReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (SqlReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}
	}

	public static class ExtendedSqlParseManager extends SqlParseManager {
		@Override
		public String getTableNames(String sqlName, String sqlStatement, String domain) {
			return parseSql(sqlStatement);
		}
	}
}
