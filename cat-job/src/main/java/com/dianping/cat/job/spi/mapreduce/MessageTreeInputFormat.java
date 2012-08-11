package com.dianping.cat.job.spi.mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class MessageTreeInputFormat extends DirectoryInputFormat<LongWritable, MessageTreeWritable> {
	@Override
	public MessageTreeReader createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException,
	      InterruptedException {
		FileSplit fs = (FileSplit) split;
		String name = fs.getPath().getName();

		if (name.endsWith(".gz")) { // version 1
			return new MessageTreeReader();
		} else { // version 2
			return new MessageTreeReaderV2();
		}
	}

	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}
}
