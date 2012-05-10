package com.dianping.cat.job;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.dianping.cat.hadoop.mapreduce.MessageTreeInputFormat;

public enum JobFactory {
	INSTANCE;

	private String m_hdfsServer = "10.1.1.169";

	public Job createJob(Object task, Configuration configuration, String[] args) throws IOException {
		Class<?> taskClass = task.getClass();
		JobMeta jobMeta = taskClass.getAnnotation(JobMeta.class);

		if (jobMeta == null) {
			throw new IllegalStateException(String.format("%s should be annotated by %s!", taskClass, JobMeta.class));
		}

		Class<? extends Mapper<?, ?, ?, ?>> mapperClass = jobMeta.mapper();
		Class<? extends Reducer<?, ?, ?, ?>> reducerClass = jobMeta.reducer();
		MapperMeta mapperMeta = mapperClass.getAnnotation(MapperMeta.class);
		ReducerMeta reducerMeta = reducerClass.getAnnotation(ReducerMeta.class);

		if (mapperMeta == null) {
			throw new RuntimeException(String.format("%s should be annotated by %s!", mapperClass, MapperMeta.class));
		} else if (reducerMeta == null) {
			throw new RuntimeException(String.format("%s should be annotated by %s!", reducerClass, ReducerMeta.class));
		} else {
			if (mapperMeta.keyOut() != reducerMeta.keyIn()) {
				throw new RuntimeException(String.format(
				      "The output key(%s) of mapper(%s) does not match the input key(%s) of reducer(%s)!",
				      mapperMeta.keyOut(), mapperClass, reducerMeta.keyIn(), reducerClass));
			} else if (mapperMeta.valueOut() != reducerMeta.valueIn()) {
				throw new RuntimeException(String.format(
				      "The output value(%s) of mapper(%s) does not match the input value(%s) of reducer(%s)!",
				      mapperMeta.valueOut(), mapperClass, reducerMeta.valueIn(), reducerClass));
			}
		}

		Job job = new Job(configuration, jobMeta.name());

		job.setJarByClass(taskClass);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setMapperClass(mapperClass);
		job.setReducerClass(reducerClass);
		job.setPartitionerClass(jobMeta.partitioner());
		job.setNumReduceTasks(jobMeta.reducerNum());

		job.setMapOutputKeyClass(mapperMeta.keyOut());
		job.setMapOutputValueClass(mapperMeta.valueOut());

		job.setOutputKeyClass(reducerMeta.keyOut());
		job.setOutputValueClass(reducerMeta.valueOut());

		validateDefaultConstructor(mapperMeta.keyOut());
		validateDefaultConstructor(mapperMeta.valueOut());
		validateDefaultConstructor(reducerMeta.keyOut());
		validateDefaultConstructor(reducerMeta.valueOut());

		// setup default input path
		String inPath = args.length > 0 ? args[0] : getDefaultInputPath(job);
		FileInputFormat.addInputPath(job, new Path(inPath));

		// setup default output path
		String outPath = getDefaultOutputPath(job);
		FileOutputFormat.setOutputPath(job, new Path(outPath));

		System.out.println("Input path: " + inPath);
		System.out.println("Output path: " + outPath);
		return job;
	}

	private String getDefaultInputPath(Job job) {
		MessageFormat inFormat = new MessageFormat("hdfs://{0}/user/cat/dump/{1,date,yyyyMMdd}/{2}");
		String hour = getLastHour();
		String inPath = inFormat.format(new Object[] { m_hdfsServer, new Date(), hour });

		return inPath;
	}

	private String getDefaultOutputPath(Job job) {
		MessageFormat outFormat = new MessageFormat("hdfs://{0}/user/cat/job/{1,date,yyyyMMdd}/{2}/{1,date,HHmmss}");
		String outPath = outFormat.format(new Object[] { m_hdfsServer, new Date(), job.getJobName() });

		return outPath;
	}

	private String getLastHour() {
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.HOUR_OF_DAY, -1);

		int hour = cal.get(Calendar.HOUR_OF_DAY);

		return hour < 10 ? "0" + hour : String.valueOf(hour);
	}

	private void validateDefaultConstructor(Class<?> clazz) {
		try {
			clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format(
			      "Default constructor should be defined in %s for data serialization!", clazz));
		}
	}
}
