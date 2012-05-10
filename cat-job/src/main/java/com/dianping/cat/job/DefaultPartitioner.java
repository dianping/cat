package com.dianping.cat.job;

import org.apache.hadoop.mapreduce.Partitioner;

public class DefaultPartitioner extends Partitioner<Object, Object> {
	@Override
	public int getPartition(Object key, Object value, int numPartitions) {
		return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
	}
}