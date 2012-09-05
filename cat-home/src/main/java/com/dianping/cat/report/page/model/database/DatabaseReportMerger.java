package com.dianping.cat.report.page.model.database;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.entity.Domain;
import com.dianping.cat.consumer.database.model.entity.Method;
import com.dianping.cat.consumer.database.model.entity.Table;
import com.dianping.cat.consumer.database.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class DatabaseReportMerger extends DefaultMerger {

	private boolean m_allDomain = false;

	private Domain m_all;

	public DatabaseReportMerger(DatabaseReport databaseReport) {
		super(databaseReport);
	}

	@Override
	public void visitDomain(Domain domain) {
		if (m_allDomain) {
			visitDomainChildren(m_all, domain);
		} else {
			super.visitDomain(domain);
		}
	}

	@Override
	protected void mergeTable(Table old, Table table) {
		double sum = 0;
		if (old.getTotalPercent() > 0) {
			sum += old.getTotalCount() / old.getTotalPercent();
		}
		if (table.getTotalPercent() > 0) {
			sum += table.getTotalCount() / table.getTotalPercent();
		}
		old.setTotalPercent((old.getTotalCount() + table.getTotalCount()) / sum);

		old.setTotalCount(old.getTotalCount() + table.getTotalCount());
		old.setFailCount(old.getFailCount() + table.getFailCount());
		old.setFailPercent(old.getFailCount() / (double) old.getTotalCount());
		old.setSum(old.getSum() + table.getSum());
		old.setAvg(old.getSum() / (double) old.getTotalCount());
	}

	@Override
	protected void mergeMethod(Method old, Method method) {
		double sum = 0;
		if (old.getTotalPercent() > 0) {
			sum += old.getTotalCount() / old.getTotalPercent();
		}
		if (method.getTotalPercent() > 0) {
			sum += method.getTotalCount() / method.getTotalPercent();
		}
		old.setTotalPercent((old.getTotalCount() + method.getTotalCount()) / sum);

		old.setTotalCount(old.getTotalCount() + method.getTotalCount());
		old.setFailCount(old.getFailCount() + method.getFailCount());
		old.setFailPercent(old.getFailCount() / (double) old.getTotalCount());
		old.setSum(old.getSum() + method.getSum());
		old.setAvg(old.getSum() / (double) old.getTotalCount());
	}

	@Override
	protected void mergeDatabaseReport(DatabaseReport old, DatabaseReport databaseReport) {
		old.getDomainNames().addAll(databaseReport.getDomainNames());
		old.getDatabaseNames().addAll(databaseReport.getDatabaseNames());
		super.mergeDatabaseReport(old, databaseReport);
	}

	@Override
	public void visitDatabaseReport(DatabaseReport databaseReport) {
		DatabaseReport old = getDatabaseReport();

		if (m_allDomain) {
			m_all = old.findOrCreateDomain(CatString.ALL_Domain);
		}
		super.visitDatabaseReport(databaseReport);
		old.getDomainNames().addAll(databaseReport.getDomainNames());
		old.getDatabaseNames().addAll(databaseReport.getDatabaseNames());
		
		if(m_allDomain){
			old.getDomainNames().remove(CatString.ALL_Domain);
		}

	}

	public void setAllDomain(boolean allDomain) {
		m_allDomain = true;
	}
}
