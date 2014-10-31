package com.dianping.cat.report.task.highload;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.report.HighloadSql;
import com.dianping.cat.home.dal.report.HighloadSqlDao;
import com.dianping.cat.home.dal.report.HighloadSqlEntity;
import com.dianping.cat.report.task.highload.HighLoadSqlUpdater.HighLoadSQLReport;

public class HighLoadService {

	@Inject
	private HighloadSqlDao m_sqlDao;

	private HighLoadSQLReport convertSql(HighloadSql dbSql) throws SAXException, IOException {
		HighLoadSQLReport sqlReport = new HighLoadSqlUpdater().new HighLoadSQLReport();
		String nameContext = dbSql.getTransactionNameContent();
		TransactionName name = DefaultSaxParser.parseEntity(TransactionName.class, nameContext);

		sqlReport.setName(name);
		sqlReport.setDate(dbSql.getDate());
		sqlReport.setDomain(dbSql.getDomain());
		sqlReport.setWeight(dbSql.getWeight());

		return sqlReport;
	}

	public List<HighLoadSQLReport> queryHighLoadSqls(Date date) throws DalException {
		List<HighLoadSQLReport> sqls = new ArrayList<HighLoadSQLReport>();
		List<HighloadSql> dbSqls = m_sqlDao.findByDate(date, HighloadSqlEntity.READSET_FULL);

		for (HighloadSql dbSql : dbSqls) {
			try {
				sqls.add(convertSql(dbSql));
			} catch (Exception e) {
				e.printStackTrace();
				Cat.logError(e);
			}
		}
		return sqls;
	}

}
