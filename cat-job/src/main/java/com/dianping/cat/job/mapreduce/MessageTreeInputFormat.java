package com.dianping.cat.job.mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.dianping.cat.job.sql.DirectoryInputFormat;

public class MessageTreeInputFormat extends DirectoryInputFormat<LongWritable, MessageTreeWritable> {
	@Override
	public RecordReader<LongWritable, MessageTreeWritable> createRecordReader(InputSplit split,
	      TaskAttemptContext context) throws IOException, InterruptedException {
		return new MessageTreeReader();
	}

	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}
}
