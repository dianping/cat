package com.dianping.cat.job.spi.joblet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.util.ReflectionUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.JobFactory;
import com.dianping.cat.job.spi.mapreduce.MessageTreeInputFormat;
import com.dianping.cat.job.spi.mapreduce.MessageTreeReader;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.site.helper.Formats;
import com.site.lookup.ContainerHolder;

public class JobletRunner extends ContainerHolder implements LogEnabled {
	private Logger m_logger;

	private void doMap(Joblet<?, ?> joblet, MessageTreeReader reader, JobletContext context) throws IOException,
	      InterruptedException {
		long start = System.currentTimeMillis();
		float[] points = { 0.9f, 0.7f, 0.5f, 0.3f, 0.1f };
		String last = "0% mapped ...";
		float lastPoint = 0;
		long time = start;

		m_logger.info(last);

		while (reader.nextKeyValue()) {
			MessageTreeWritable value = reader.getCurrentValue();

			joblet.map(context, value);

			long now = System.currentTimeMillis();

			if (now - time > 1000) {
				time = now;

				float progress = reader.getProgress();

				for (float point : points) {
					if (progress >= point && lastPoint < point) {
						String current = (int) (point * 100) + "% mapped ...";

						if (!current.equals(last)) {
							last = current;
							lastPoint = point;
							m_logger.info(last);
						}
					}
				}
			}
		}

		long end = System.currentTimeMillis();
		String rate = end == start ? "N/A" : Formats.forNumber().format(reader.getFileLength() * 1000.0 / (end - start),
		      "0.#", "B/s");

		m_logger.info(String.format("%s mapped in %s ms at %s.", "100%", end - start, rate));
	}

	private void doReduce(Joblet<Object, Object> joblet, Map<Object, List<Object>> map, JobletContext context)
	      throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		float[] points = { 0.9f, 0.7f, 0.5f, 0.3f, 0.1f };
		String last = "0% reduced ...";
		float lastPoint = 0;
		int count = map.size();
		int index = 0;
		long time = start;

		m_logger.info(last);

		for (Map.Entry<Object, List<Object>> e : map.entrySet()) {
			joblet.reduce(context, e.getKey(), e.getValue());
			index++;

			long now = System.currentTimeMillis();

			if (now - time > 1000) {
				time = now;

				float progress = index * 1.0f / count;

				for (float point : points) {
					if (progress >= point && lastPoint < point) {
						String current = (int) (point * 100) + "% reduced ...";

						if (!current.equals(last)) {
							last = current;
							lastPoint = point;
							m_logger.info(last);
						}
					}
				}
			}
		}

		long end = System.currentTimeMillis();
		String rate = end == start ? "N/A" : Formats.forNumber().format(count * 1000.0 / (end - start), "0.#", " Rows/s");

		m_logger.info(String.format("%s reduced in %s ms at %s.", "100%", end - start, rate));
	}

	private int run(Joblet<Object, Object> joblet, JobCmdLine cmdLine) throws Exception {
		Job job = JobFactory.INSTANCE.createJob(joblet, new Configuration(), cmdLine);
		Configuration conf = job.getConfiguration();
		MessageTreeInputFormat inputFormat = (MessageTreeInputFormat) ReflectionUtils.newInstance(
		      job.getInputFormatClass(), conf);
		TaskAttemptID id = new TaskAttemptID(cmdLine.getJobletName(), 0, true, 0, 0);
		TaskAttemptContext context = new TaskAttemptContext(conf, id);
		List<InputSplit> splits = inputFormat.getSplits(context);
		MapJobletContext mapperContext = new MapJobletContext();
		OutputFormat<?, ?> outputFormat = ReflectionUtils.newInstance(job.getOutputFormatClass(), conf);
		RecordWriter<?, ?> writer = outputFormat.getRecordWriter(context);

		for (InputSplit split : splits) {
			MessageTreeReader reader = inputFormat.createRecordReader(split, context);

			reader.initialize(split, context);
			doMap(joblet, reader, mapperContext);
		}

		ReducerContext reducerContext = new ReducerContext(writer);

		doReduce(joblet, mapperContext.getMap(), reducerContext);

		writer.close(context);
		return 0;
	}

	@SuppressWarnings("unchecked")
	public int run(String jobletName, String... otherArgs) throws Exception {
		Joblet<Object, Object> joblet = lookup(Joblet.class, jobletName);
		String[] args = new String[otherArgs.length + 1];

		args[0] = jobletName;
		System.arraycopy(otherArgs, 0, args, 1, otherArgs.length);

		JobCmdLine cmdLine = new JobCmdLine(args);

		if (joblet.initialize(cmdLine)) {
			return run(joblet, cmdLine);
		} else {
			return -1;
		}
	}

	static class ReducerContext implements JobletContext {
		private RecordWriter<Object, Object> m_writer;

		@SuppressWarnings("unchecked")
		public ReducerContext(RecordWriter<?, ?> writer) {
			m_writer = (RecordWriter<Object, Object>) writer;
		}

		@Override
		public void write(Object key, Object value) throws IOException, InterruptedException {
			m_writer.write(key, value);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
