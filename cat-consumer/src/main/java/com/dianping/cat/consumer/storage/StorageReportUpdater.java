package com.dianping.cat.consumer.storage;

import java.util.Set;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;

public class StorageReportUpdater {

	public void updateStorageIds(String id, Set<String> ids, StorageReport report) {
		String type = id.substring(id.lastIndexOf("-") + 1);
		Set<String> reportIds = report.getIds();
		reportIds.clear();

		for (String myId : ids) {
			if (myId.endsWith(type)) {
				String prefix = myId.substring(0, myId.lastIndexOf("-"));

				reportIds.add(prefix);
			}
		}
	}

	public void updateStorageReport(StorageReport report, StorageUpdateParam param) {
		Transaction t = param.getTransaction();
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		long duration = t.getDurationInMillis();
		Domain d = report.findOrCreateMachine(param.getIp()).findOrCreateDomain(param.getDomain());
		Operation operation = d.findOrCreateOperation(param.getMethod());
		Segment segment = operation.findOrCreateSegment(min);

		report.addIp(param.getIp());
		report.addOp(param.getMethod());

		operation.incCount();
		operation.incSum(duration);
		operation.setAvg(operation.getSum() / operation.getCount());

		segment.incCount();
		segment.incSum(duration);
		segment.setAvg(segment.getSum() / segment.getCount());

		if (!t.isSuccess()) {
			operation.incError();
			segment.incError();
		} else {
			// updateSqlInfo(param, d);
		}
		if (duration > param.getThreshold()) {
			operation.incLongCount();
			segment.incLongCount();
		}
	}

	// private void updateSqlInfo(StorageUpdateParam param, Domain d) {
	// String sqlName = param.getSqlName();
	//
	// if (StringUtils.isNotEmpty(sqlName)) {
	// Sql sql = d.findOrCreateSql(sqlName);
	// String sqlStatement = sql.getStatement();
	//
	// if (StringUtils.isEmpty(sqlStatement)) {
	// sqlStatement = sqlStatement.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	//
	// sql.setStatement(sqlStatement);
	// }
	// sql.incCount();
	// }
	// }

	public static class StorageUpdateParam {

		private String m_id;

		private String m_ip;

		private String m_domain;

		private String m_method;

		private String m_sqlName;

		private String m_sqlStatement;

		private Transaction m_transaction;

		private long m_threshold;

		public String getDomain() {
			return m_domain;
		}

		public String getId() {
			return m_id;
		}

		public String getIp() {
			return m_ip;
		}

		public String getMethod() {
			return m_method;
		}

		public String getSqlName() {
			return m_sqlName;
		}

		public String getSqlStatement() {
			return m_sqlStatement;
		}

		public long getThreshold() {
			return m_threshold;
		}

		public Transaction getTransaction() {
			return m_transaction;
		}

		public StorageUpdateParam setDomain(String domain) {
			m_domain = domain;
			return this;
		}

		public StorageUpdateParam setId(String id) {
			m_id = id;
			return this;
		}

		public StorageUpdateParam setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public StorageUpdateParam setMethod(String method) {
			m_method = method;
			return this;
		}

		public StorageUpdateParam setSqlName(String sqlName) {
			m_sqlName = sqlName;
			return this;
		}

		public StorageUpdateParam setSqlStatement(String sqlStatement) {
			m_sqlStatement = sqlStatement;
			return this;
		}

		public StorageUpdateParam setThreshold(long threshold) {
			m_threshold = threshold;
			return this;
		}

		public StorageUpdateParam setTransaction(Transaction transaction) {
			m_transaction = transaction;
			return this;
		}
	}
}
