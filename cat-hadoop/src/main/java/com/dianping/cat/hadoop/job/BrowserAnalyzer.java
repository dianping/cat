package com.dianping.cat.hadoop.job;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.dianping.cat.hadoop.mapreduce.MessageTreeInputFormat;
import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.Message;
import com.site.helper.Files;

public class BrowserAnalyzer extends Configured implements Tool {
	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new BrowserAnalyzer(), args);

		System.exit(exitCode);
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();

		Job job = new Job(conf, "browser analyzer");
		job.setJarByClass(BrowserAnalyzer.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path("target/hdfs/20120215/17/null"));
		FileOutputFormat.setOutputPath(job, new Path("target/browser"));

		Files.forDir().delete(new File("target/browser"), true);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
		      InterruptedException {
			int sum = 0;

			for (IntWritable val : values) {
				sum += val.get();
			}

			result.set(sum);
			context.write(key, result);
		}
	}

	public static class TokenizerMapper extends Mapper<Object, MessageTreeWritable, Text, IntWritable> {
		private final static IntWritable ONE = new IntWritable(1);

		private Text m_word = new Text();

		public void map(Object key, MessageTreeWritable value, Context context) throws IOException, InterruptedException {
			Message message = value.get().getMessage();

			m_word.set(message.getType() + "." + message.getName());
			context.write(m_word, ONE);
		}
	}
}
