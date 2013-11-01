package com.dianping.cat.consumer.sql;

import java.util.Stack;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.sql.model.entity.Method;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.entity.Table;
import com.dianping.cat.consumer.sql.model.transform.DefaultMerger;

public class SqlReportMerger extends DefaultMerger {

	private boolean m_allDatabase = false;

	public SqlReportMerger(SqlReport sqlReport) {
		super(sqlReport);
	}

	@Override
	protected void visitSqlReportChildren(SqlReport to, SqlReport from) {
		Stack<Object> objs = getObjects();
		for (Database source : from.getDatabases().values()) {
			Database target;

			if (m_allDatabase) {
				target = to.findOrCreateDatabase(Constants.ALL);
			} else {
				target = to.findOrCreateDatabase(source.getId());
			}

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

	@Override
	protected void mergeDatabase(Database to, Database from) {
		if (!m_allDatabase) {
			super.mergeDatabase(to, from);
		}
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
		Database machine = new Database(Constants.ALL);

		for (Database m : report.getDatabases().values()) {
			if (!m.getId().equals(Constants.ALL)) {
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
	public void visitSqlReport(SqlReport sqlReport) {
		SqlReport old = getSqlReport();
		super.visitSqlReport(sqlReport);
		old.setStartTime(sqlReport.getStartTime());
		old.setEndTime(sqlReport.getEndTime());
		old.getDatabaseNames().addAll(sqlReport.getDatabaseNames());
		old.getDomainNames().addAll(sqlReport.getDomainNames());
		if (m_allDatabase) {
			old.getDatabaseNames().remove(Constants.ALL);
		}
	}
}
