package com.dianping.cat.job.sql;

import org.apache.hadoop.mapreduce.Partitioner;

public class SqlJobPatitioner extends Partitioner<SqlStatementKey, SqlStatementValue> {

	@Override
	public int getPartition(SqlStatementKey key, SqlStatementValue value, int numPartitions) {
		return key.getDomain().hashCode() % numPartitions;
	}
}
