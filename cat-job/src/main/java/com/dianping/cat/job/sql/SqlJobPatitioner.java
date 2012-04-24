package com.dianping.cat.job.sql;

import org.apache.hadoop.mapreduce.Partitioner;

public class SqlJobPatitioner extends Partitioner<SqlStatementKey, SqlStatementValue> {

	@Override
	public int getPartition(SqlStatementKey key, SqlStatementValue value, int numPartitions) {
		int hashCode = key.getDomain().hashCode();
		hashCode = Math.abs(hashCode);
		return hashCode % numPartitions;
	}
}
