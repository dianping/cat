package com.dianping.cat.report.page.model.sql;

import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.sql.model.entity.Method;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.entity.Table;
import com.dianping.cat.consumer.sql.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class SqlReportMerger extends DefaultMerger {

	private boolean m_allDatabase = false;

	private Database m_all;

	public SqlReportMerger(SqlReport sqlReport) {
		super(sqlReport);
	}

	@Override
	protected void mergeDatabase(Database old, Database database) {
		old.setConnectUrl(database.getConnectUrl());
		super.mergeDatabase(old, database);
	}

	@Override
	protected void mergeMethod(Method old, Method method) {
		old.setTotalCount(old.getTotalCount() + method.getTotalCount());
		old.setFailCount(old.getFailCount() + method.getFailCount());
		old.setFailPercent(old.getFailCount() / (double) old.getTotalCount());
		old.setSum(old.getSum() + method.getSum());
		old.setAvg(old.getSum() / (double) old.getTotalCount());

		old.getSqlNames().addAll(method.getSqlNames());
	}

	public Database mergesForAllMachine(SqlReport report) {
		Database machine = new Database(CatString.ALL);

		for (Database m : report.getDatabases().values()) {
			if (!m.getId().equals(CatString.ALL)) {
				visitDatabaseChildren(machine, m);
			}
		}

		return machine;
	}

	@Override
	protected void mergeTable(Table old, Table table) {
		old.setTotalCount(old.getTotalCount() + table.getTotalCount());
		old.setFailCount(old.getFailCount() + table.getFailCount());
		old.setFailPercent(old.getFailCount() / (double) old.getTotalCount());
		old.setSum(old.getSum() + table.getSum());
		old.setAvg(old.getSum() / (double) old.getTotalCount());
	}

	public void setAllDatabase(boolean allDatabase) {
		m_allDatabase = true;
	}

	@Override
	public void visitDatabase(Database domain) {
		if (m_allDatabase) {
			visitDatabaseChildren(m_all, domain);
		} else {
			super.visitDatabase(domain);
		}
	}

	@Override
	public void visitSqlReport(SqlReport sqlReport) {
		SqlReport old = getSqlReport();

		if (m_allDatabase) {
			m_all = old.findOrCreateDatabase(CatString.ALL);
		}
		super.visitSqlReport(sqlReport);
		old.setStartTime(sqlReport.getStartTime());
		old.setEndTime(sqlReport.getEndTime());
		old.getDatabaseNames().addAll(sqlReport.getDatabaseNames());
		old.getDomainNames().addAll(sqlReport.getDomainNames());
		if (m_allDatabase) {
			old.getDatabaseNames().remove(CatString.ALL);
		}
	}
}
