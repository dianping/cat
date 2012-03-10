package com.dianping.cat.job.sql;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.dianping.cat.job.mapreduce.MessageTreeInputFormat;
import com.dianping.cat.job.sql.database.SqlRecordJobMapper;
import com.dianping.cat.job.sql.database.SqlRecordJobReducer;
import com.site.helper.Files;

public class SqlJobMain extends Configured implements Tool {

	private static final String DEFAUL_IN_PATH = "target/hdfs/";

	private static final String DEFAUL_OUT_PATH = "target/cat/sql/";

	private static final String DEFAULT_FINAL_PATH = "target/cat/result/";

	private static final int DEFAULT_REDUCE_NUMBER = 3;

	/**
	 * The job process last hour data when no args default. The args[0] can set
	 * the number of reduce; The args[1] is for input path
	 */
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new SqlJobMain(), args);

		System.exit(exitCode);
	}

	private String getLastHoursString(int hours) {
		Date date = new Date();
		long lastHour = date.getTime();

		lastHour = lastHour - lastHour % (60 * 60 * 1000) - 60 * 60 * 1000 * hours;
		date.setTime(lastHour);
		return new SimpleDateFormat("yyyyMMdd/HH/").format(date);
	}

	@Override
	public int run(String[] args) throws Exception {
		/*Configuration conf = getConf();

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
		job.setNumReduceTasks(DEFAULT_REDUCE_NUMBER);

		if (args.length > 0) {
			try {
				job.setNumReduceTasks(Integer.parseInt(args[0]));
			} catch (Exception e) {
				System.out.println("The input args of the job is not correct, the args[0] should be integer!");
				return 0;
			}
		}
		String hourStr = getLastHoursString(1);

		String inputPath = DEFAUL_IN_PATH + hourStr;
		String outputPath = DEFAUL_OUT_PATH + hourStr;

		if (args.length > 1) {
			if (args.length >= 2) {
				inputPath = args[1];
			}
		}

		System.out.println(String.format("InputPath: %s,OutPath %s", inputPath, outputPath));

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		Files.forDir().delete(new File(outputPath), true);

		if (job.waitForCompletion(true)) {
			return runSqlRecordJob(hourStr);
		} else {
			return 0;
		}*/
		String hourStr = getLastHoursString(1);
		return runSqlRecordJob(hourStr);
	}

	/*
	 * insert the result to mysql
	 */
	private int runSqlRecordJob(String currentHour) throws Exception {
		Configuration conf = getConf();
		conf.set("JobHour", currentHour);
		Job job = new Job(conf, "Sql Record");

		job.setJarByClass(SqlJobMain.class);
		job.setMapperClass(SqlRecordJobMapper.class);
		job.setReducerClass(SqlRecordJobReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(DEFAUL_OUT_PATH+currentHour));
		FileOutputFormat.setOutputPath(job, new Path(DEFAULT_FINAL_PATH));
		Files.forDir().delete(new File(DEFAULT_FINAL_PATH), true);
		return job.waitForCompletion(true) ? 0 : 1;
	}
}
