package com.dianping.cat.job.url;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class UrlJobPatitioner extends Partitioner<Text, UrlValue> {

	@Override
	public int getPartition(Text key, UrlValue value, int numPartitions) {
		int hashCode = key.hashCode();
		hashCode = Math.abs(hashCode);
		return hashCode % numPartitions;
	}
}
