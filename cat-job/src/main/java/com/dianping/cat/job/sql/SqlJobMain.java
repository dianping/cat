package com.dianping.cat.job.sql;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.dianping.cat.job.mapreduce.MessageTreeInputFormat;
import com.site.helper.Files;

public class SqlJobMain extends Configured implements Tool {
	
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new SqlJobMain(), args);

		System.exit(exitCode);
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();

		Job job = new Job(conf, "Sql Analyzer");
		job.setJarByClass(SqlJobMain.class);
		job.setMapperClass(SqlJobMapper.class);
		job.setReducerClass(SqlJobReducer.class);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setOutputKeyClass(SqlStatementKey.class);
		job.setOutputValueClass(SqlJobResult.class);
		job.setPartitionerClass(SqlJobPatitioner.class);
		
		job.setMapOutputKeyClass(SqlStatementKey.class);
		job.setMapOutputValueClass(SqlStatementValue.class);
		job.setNumReduceTasks(2);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/HH/");
		String dateStr = sdf.format(new Date());
		String path = "target/hdfs/20120306/23/null/";
		
		FileInputFormat.addInputPath(job, new Path(path));
		FileOutputFormat.setOutputPath(job, new Path("target/sql"));
		Files.forDir().delete(new File("target/sql"), true);
		
		return job.waitForCompletion(true) ? 0 : 1;
	}
}
