package com.dianping.cat.job.url;

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

import com.dianping.cat.hadoop.mapreduce.MessageTreeInputFormat;

public class UrlJobMain extends Configured implements Tool {

	private static String BASE_URL;

	private static String DEFAULT_IN_PATH = "hdfs://10.1.1.169/user/cat/dump/";

	private static String DEFAULT_OUT_PATH = "hdfs://10.1.1.169/user/cat/url/";

	private static final int DEFAULT_REDUCE_NUMBER = 3;

	/**
	 * The job process last hour data when no args default. The args[0] can set
	 * the number of reduce; The args[1] is for input path
	 */
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new UrlJobMain(), args);

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
		Job job = new Job(conf, "Cat_Url_Job");

		job.setJarByClass(UrlJobMain.class);
		job.setMapperClass(UrlJobMapper.class);
		job.setReducerClass(UrlJobReducer.class);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(UrlValue.class);
		
		job.setPartitionerClass(UrlJobPatitioner.class);
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

		//for local mode
		if (args.length >= 3) {
			BASE_URL = args[2];
			if (BASE_URL.charAt(BASE_URL.length() - 1) == '/') {
				BASE_URL = BASE_URL.substring(0, BASE_URL.length() - 1);
			}
			DEFAULT_IN_PATH = BASE_URL;
			DEFAULT_OUT_PATH = BASE_URL + "/url/";
		}

		String inputPath = DEFAULT_IN_PATH + hourStr;
		String outputPath = DEFAULT_OUT_PATH + hourStr;

		System.out.println(String.format("InputPath: %s , OutPath %s", inputPath, outputPath));

		FileInputFormat.addInputPath(job, new Path(inputPath));
		Path outPath = new Path(outputPath);

		FileSystem fs = FileSystem.get(conf);
		fs.delete(outPath, true);
		FileOutputFormat.setOutputPath(job, outPath);

		return job.waitForCompletion(true) == true ? 1 : 0;
	}
}
