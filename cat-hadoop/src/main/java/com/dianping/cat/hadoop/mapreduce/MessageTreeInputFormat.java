package com.dianping.cat.hadoop.mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class MessageTreeInputFormat extends FileInputFormat<LongWritable, MessageTreeWritable> {
	@Override
	public RecordReader<LongWritable, MessageTreeWritable> createRecordReader(InputSplit split,
	      TaskAttemptContext context) throws IOException, InterruptedException {
		return new MessageTreeReader();
	}

	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		// the file is already small enough, so do not need to split it
		return false;
	}
}
