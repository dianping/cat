package com.dianping.cat.job.joblet;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.job.joblet.SqlJoblet.Sql;
import com.dianping.cat.job.joblet.SqlJoblet.SqlStat;
import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletContext;
import com.dianping.cat.job.spi.joblet.JobletMeta;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.spi.mapreduce.PojoWritable;
import com.dianping.cat.job.sql.SqlJobResult;
import com.dianping.cat.job.sql.dal.SqlReportRecord;
import com.dianping.cat.job.sql.dal.SqlReportRecordDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Formats;
import com.site.lookup.annotation.Inject;

@JobletMeta(name = "sql", description = "Sql analysis", keyClass = Sql.class, valueClass = SqlStat.class)
public class SqlJoblet implements Joblet<Sql, SqlStat> {
	private static final String DEFAULT_DOMAIN = "NoDomain";

	@Inject
	private SqlOutputter m_outputter;

	@Override
	public boolean initialize(JobCmdLine cmdLine) {
		String inputPath = cmdLine.getArg("inputPath", 0, null);
		String outputPath = cmdLine.getArg("outputPath", 1, null);

		if (inputPath != null) {
			cmdLine.setProperty("inputPath", inputPath);
		}

		if (outputPath != null) {
			cmdLine.setProperty("outputPath", outputPath);
		}

		return true;
	}

	@Override
	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException {
		MessageTree tree = treeWritable.get();
		Message message = tree.getMessage();
		String domain = tree.getDomain();

		if (domain == null || domain.length() == 0) {
			domain = DEFAULT_DOMAIN;
		}

		if (message instanceof Transaction) {
			Transaction root = (Transaction) message;

			processTransaction(context, root, root, tree, domain);
		}
	}

	private void processTransaction(JobletContext context, Transaction transaction, Transaction root, MessageTree tree,
	      String domain) throws IOException, InterruptedException {
		String type = transaction.getType();

		if (type.equals("SQL")) {
			Sql sql = new Sql();
			String name = transaction.getName();
			String statement = transaction.getData().toString();
			long duration = transaction.getDurationInMillis();
			int flag = 0;

			sql.setDomain(domain).setName(name).setStatement(statement.trim());

			if (!transaction.getStatus().equals(Transaction.SUCCESS)) {
				flag = 1;
			}

			long transationDate = root.getTimestamp();
			long timeInMinute = transationDate - transationDate % (60 * 1000);
			int minute = (int) ((timeInMinute % (60 * 60 * 1000)) / (60 * 1000));
			SqlStat result = new SqlStat(flag, duration, minute);

			sql.setTransationDate(root.getTimestamp());
			context.write(sql, result);
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction child = (Transaction) message;

				processTransaction(context, child, root, tree, domain);
			}
		}
	}

	@Override
	public void reduce(JobletContext context, Sql key, Iterable<SqlStat> values) throws IOException,
	      InterruptedException {
		SqlJobResult result = new SqlJobResult();

		for (SqlStat val : values) {
			result.add(val.getValue(), val.getFlag(), null, val.getMinute());
		}

		m_outputter.out(context, key, result);
	}

	public static class Sql extends PojoWritable {
		private String m_name;

		private String m_domain;

		@FieldMeta(key = false)
		private String m_statement;

		private long m_transactionDate;

		public Sql() {
		}

		public String getDomain() {
			return m_domain;
		}

		public String getName() {
			return m_name;
		}

		public String getStatement() {
			return m_statement;
		}

		public long getTransactionDate() {
			return m_transactionDate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();

			result = prime * result + ((m_domain == null) ? 0 : m_domain.hashCode());
			result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());

			return result;
		}

		public Sql setDomain(String domain) {
			m_domain = domain;
			return this;
		}

		public Sql setName(String name) {
			m_name = name;
			return this;
		}

		public Sql setStatement(String statement) {
			m_statement = statement;
			return this;
		}

		public void setTransationDate(long transationDate) {
			m_transactionDate = transationDate;
		}
	}

	public static class SqlDatabaseOutputter implements SqlOutputter, LogEnabled {
		@Inject
		private SqlReportRecordDao m_dao;

		private Logger m_logger;

		@Override
		public void enableLogging(Logger logger) {
			m_logger = logger;
		}

		private String map2String(Map<?, ?> map) {
			StringBuilder sb = new StringBuilder();

			if (map != null) {
				for (Entry<?, ?> entry : map.entrySet()) {
					sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
				}

				sb.deleteCharAt(sb.length() - 1);
			}

			return sb.toString();
		}

		@Override
		public void out(JobletContext context, Sql key, SqlJobResult value) {
			try {
				SqlReportRecord row = m_dao.createLocal();

				row.setDomain(key.getDomain());
				row.setTotalCount(value.getDurations().size());
				row.setFailureCount(value.getFailureCount());
				row.setLongSqls(value.getLongTimeCount());
				row.setAvg2Value(value.getPercent95Line());
				row.setSumValue(value.getSum());
				row.setSum2Value(value.getSum2());
				row.setMaxValue(value.getMax());
				row.setMinValue(value.getMin());
				row.setStatement(Formats.forObject().shorten(key.getStatement(), 2000));
				row.setName(key.getName());
				row.setSampleLink("");
				row.setTransactionDate(new Date(key.getTransactionDate()));
				row.setDurationDistribution(map2String(value.getDurationDistribution()));
				row.setHitsOverTime(map2String(value.getHitsOverTime()));
				row.setDurationOverTime(map2String(value.getDurationOverTime()));
				row.setFailureOverTime(map2String(value.getFailureOverTime()));

				m_dao.insert(row);

				m_logger.info(String.format("Add statement(%s) successfully.", key.getName()));
			} catch (Exception e) {
				m_logger.error(e.toString());
			}
		}
	}

	public static interface SqlOutputter {
		public void out(JobletContext context, Sql key, SqlJobResult value);
	}

	public static class SqlStat extends PojoWritable {
		public int m_flag;

		public double m_value;

		public int m_minute;

		public SqlStat() {
		}

		public SqlStat(int flag, double value, int minute) {
			m_flag = flag;
			m_value = value;
			m_minute = minute;
		}

		public int getFlag() {
			return m_flag;
		}

		public int getMinute() {
			return m_minute;
		}

		public double getValue() {
			return m_value;
		}
	}
}
