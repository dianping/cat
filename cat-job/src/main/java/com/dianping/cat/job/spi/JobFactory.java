package com.dianping.cat.job.spi;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletMapper;
import com.dianping.cat.job.spi.joblet.JobletMeta;
import com.dianping.cat.job.spi.joblet.JobletReducer;
import com.dianping.cat.job.spi.mapreduce.MessageTreeInputFormat;
import com.site.helper.Files;

public enum JobFactory {
	INSTANCE;

	private String m_hdfsServer = "10.1.1.169";

	public Job createJob(Joblet<?, ?> joblet, Configuration configuration, JobCmdLine cmdLine) throws IOException {
		Class<?> jobletClass = joblet.getClass();
		JobletMeta meta = jobletClass.getAnnotation(JobletMeta.class);

		if (meta == null) {
			throw new IllegalStateException(String.format("%s should be annotated by %s!", jobletClass, JobletMeta.class));
		}

		Job job = new Job(configuration, meta.name());

		if (!joblet.initialize(cmdLine)) {
			return null;
		}

		job.getConfiguration().setClass("joblet.class", joblet.getClass(), Joblet.class);

		job.setJarByClass(jobletClass);
		job.setInputFormatClass(MessageTreeInputFormat.class);
		job.setMapperClass(JobletMapper.class);
		job.setReducerClass(JobletReducer.class);

		if (meta.combine()) {
			job.setCombinerClass(JobletReducer.class);
		}

		if (meta.partitioner() != JobletMeta.DefaultPartitioner.class) {
			job.setPartitionerClass(meta.partitioner());
		}

		job.setNumReduceTasks(cmdLine.getPropertyInt("reducers", meta.reducerNum()));

		job.setMapOutputKeyClass(meta.keyClass());
		job.setMapOutputValueClass(meta.valueClass());

		job.setOutputKeyClass(meta.keyClass());
		job.setOutputValueClass(meta.valueClass());

		validateDefaultConstructor(meta.keyClass());
		validateDefaultConstructor(meta.valueClass());

		// setup default input path
		String inPath = cmdLine.getProperty("inputPath", getDefaultInputPath());
		FileInputFormat.addInputPath(job, new Path(inPath));

		// setup default output path
		String outPath = cmdLine.getProperty("outputPath", null);

		if (outPath == null) {
			if (!inPath.startsWith("hdfs://")) {
				outPath = "target/" + job.getJobName() + "-out";
			} else {
				outPath = getDefaultOutputPath(job.getJobName());
			}
		}

		FileOutputFormat.setOutputPath(job, new Path(outPath));

		if (!outPath.startsWith("hdfs://")) {
			Files.forDir().delete(new File(outPath), true);
		}

		System.out.println("Input path: " + inPath);
		System.out.println("Output path: " + outPath);
		return job;
	}

	private String getDefaultInputPath() {
		MessageFormat inFormat = new MessageFormat("hdfs://{0}/user/cat/dump/{1,date,yyyyMMdd}/{2}");
		String hour = getLastHour();
		String inPath = inFormat.format(new Object[] { m_hdfsServer, new Date(), hour });

		return inPath;
	}

	private String getDefaultOutputPath(String name) {
		MessageFormat outFormat = new MessageFormat("hdfs://{0}/user/cat/job/{1,date,yyyyMMdd}/{2}/{1,date,HHmmss}");
		String outPath = outFormat.format(new Object[] { m_hdfsServer, new Date(), name });

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
