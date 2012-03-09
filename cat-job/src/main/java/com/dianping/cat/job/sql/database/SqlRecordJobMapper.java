package com.dianping.cat.job.sql.database;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SqlRecordJobMapper extends Mapper<Object, Text, Text, Text> {
	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		context.write(value, new Text(context.getConfiguration().get("JobHour", "")));
	}
}

