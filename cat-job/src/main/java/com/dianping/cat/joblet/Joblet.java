package com.dianping.cat.joblet;

import java.io.IOException;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.JobCmdLine;

public interface Joblet<KEY, VALUE> {
	public boolean initialize(JobCmdLine cmdLine);

	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException;

	public void reduce(JobletContext context, KEY key, Iterable<VALUE> values) throws IOException, InterruptedException;
}
