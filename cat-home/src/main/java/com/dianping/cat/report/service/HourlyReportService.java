package com.dianping.cat.report.service;

import java.util.Date;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public interface HourlyReportService {

	public TransactionReport queryTransactionReport(String domain, Date start, Date end);

	public EventReport queryEventReport(String domain, Date start, Date end);

	public ProblemReport queryProblemReport(String domain, Date start, Date end);

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end);

	public MatrixReport queryMatrixReport(String domain, Date start, Date end);

	public CrossReport queryCrossReport(String domain, Date start, Date end);

	public SqlReport querySqlReport(String domain, Date start, Date end);

	public DatabaseReport queryDatabaseReport(String database, Date start, Date end);

	public HealthReport queryHealthReport(String domain, Date start, Date end);
}
