package com.dianping.cat.job.spi.joblet;

import java.io.IOException;

import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;

public interface Joblet<KEY, VALUE> {
	public boolean initialize(JobCmdLine cmdLine);

	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException;

	public void reduce(JobletContext context, KEY key, Iterable<VALUE> values) throws IOException, InterruptedException;

	public void summary();
}
