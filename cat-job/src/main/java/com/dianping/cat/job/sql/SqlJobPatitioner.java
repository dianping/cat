package com.dianping.cat.job.sql;

import org.apache.hadoop.mapreduce.Partitioner;

public class SqlJobPatitioner extends Partitioner<UrlStatementKey, UrlStatementValue> {

	@Override
	public int getPartition(UrlStatementKey key, UrlStatementValue value, int numPartitions) {
		int hashCode = key.getDomain().hashCode();
		if (hashCode > 0) {
			return hashCode % numPartitions;
		} else {
			return (-hashCode) % numPartitions;
		}
	}
}
