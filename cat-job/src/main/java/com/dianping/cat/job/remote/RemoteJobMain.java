package com.dianping.cat.job.remote;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.dianping.cat.job.remote.sql.RemoteJobSqlMapper;
import com.dianping.cat.job.remote.sql.RemoteJobSqlReducer;
import com.dianping.cat.job.spi.mapreduce.MessageTreeInputFormat;
import com.dianping.cat.job.sql.SqlJobMapper;
import com.dianping.cat.job.sql.SqlJobPatitioner;
import com.dianping.cat.job.sql.SqlJobReducer;
import com.dianping.cat.job.sql.SqlJobResult;
import com.dianping.cat.job.sql.UrlStatementKey;
import com.dianping.cat.job.sql.UrlStatementValue;

public class RemoteJobMain extends Configured implements Tool {

	private String baseUrl = "hdfs://10.1.1.169/user/cat/";

	private String defaultDumpPath = "hdfs://10.1.1.169/user/cat/dump/";

	private String defaultRemotePath = "hdfs://10.1.1.169/user/cat/remote/";

	private String defaultOutPath = "hdfs://10.1.1.169/user/cat/temp/remote";

	private String defaultSqlPath = "hdfs://10.1.1.169/user/cat/temp/sql";

	private final int DEFAULT_REDUCE_NUMBER = 3;

	/**
	 * The job process last hour data when no args default. The args[0] can set
	 * the number of reduce; The args[1] is for input path
	 */
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new RemoteJobMain(), args);

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
		Configuration conf = getConf();

		String value = "";
		conf.set("tmpfiles", value);

		FileSystem fs = FileSystem.getLocal(conf);

		Job job = new Job(conf, "Cat_Remote_Job");

		job.setJarByClass(RemoteJobMain.class);
		job.setMapperClass(SqlJobMapper.class);
		job.setReducerClass(SqlJobReducer.class);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setOutputKeyClass(UrlStatementKey.class);
		job.setOutputValueClass(SqlJobResult.class);
		job.setMapOutputKeyClass(UrlStatementKey.class);
		job.setMapOutputValueClass(UrlStatementValue.class);

		job.setPartitionerClass(SqlJobPatitioner.class);
		job.setNumReduceTasks(DEFAULT_REDUCE_NUMBER);

		if (args.length >= 1) {
			try {
				job.setNumReduceTasks(Integer.parseInt(args[0]));
			} catch (Exception e) {
				System.out.println("The input args of the job is not correct, the args[0] should be integer!");
				return 0;
			}
		}

		String hourStr = getLastHoursString(1);
		if (args.length >= 2) {
			hourStr = args[1];
		}

		// for local mode
		if (args.length >= 3) {
			baseUrl = args[2];
			if (baseUrl.charAt(baseUrl.length() - 1) == '/') {
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			}
			defaultDumpPath = baseUrl;
			defaultOutPath = baseUrl + "/sql/";
			defaultSqlPath = baseUrl + "/sqlResult/";
		}

		String inputPath = defaultDumpPath + hourStr;
		String outputPath = defaultOutPath + hourStr;
		String cachePath = defaultRemotePath + hourStr;

		System.out.println(String.format("InputPath: %s , OutPath %s", inputPath, outputPath));
		System.out.println(String.format("CachePath: %s ", cachePath));

		FileInputFormat.addInputPath(job, new Path(inputPath));

		Path outPath = new Path(outputPath);

		fs.delete(outPath, true);
		FileOutputFormat.setOutputPath(job, outPath);

		if (job.waitForCompletion(true)) {
			return runInsertIndexJob(hourStr);
		} else {
			return 0;
		}
	}

	/*
	 * insert the result to mysql
	 */
	private int runInsertIndexJob(String currentHour) throws Exception {
		System.out.println("Insert logview index to database job start!");
		Configuration conf = getConf();
		conf.set("JobHour", currentHour);
		Job job = new Job(conf, "Cat_Insert_Logview_Index");

		job.setJarByClass(RemoteJobMain.class);
		job.setMapperClass(RemoteJobSqlMapper.class);
		job.setReducerClass(RemoteJobSqlReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(defaultOutPath + currentHour));
		FileOutputFormat.setOutputPath(job, new Path(defaultSqlPath));
		FileSystem fs = FileSystem.get(conf);
		fs.delete(new Path(defaultSqlPath), true);
		return job.waitForCompletion(true) ? 0 : 1;
	}
}
