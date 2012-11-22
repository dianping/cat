package com.dianping.cat.job.spi.joblet;

import java.io.IOException;
import java.util.Arrays;
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
import org.unidal.helper.Formats;
import org.unidal.lookup.ContainerHolder;

public class JobletRunner extends ContainerHolder implements LogEnabled {
	private Logger m_logger;

	private void doMap(final Joblet<?, ?> joblet, final MessageTreeReader reader, final JobletContext context)
	      throws IOException, InterruptedException {
		float[] points = { 0.9f, 0.7f, 0.5f, 0.3f, 0.1f };
		final Progress progress = new Progress(m_logger, "mapped", points);

		while (reader.nextKeyValue()) {
			MessageTreeWritable value = reader.getCurrentValue();

			joblet.map(context, value);

			progress.tick(reader.getProgress());
		}

		long duration = progress.getDuration();
		String rate = duration == 0 ? "N/A" : Formats.forNumber().format(reader.getFileLength() * 1000.0 / duration,
		      "0.#", "B/s");

		progress.finish("mapped", rate);
	}

	private void doReduce(Joblet<Object, Object> joblet, Map<Object, List<Object>> map, JobletContext context)
	      throws IOException, InterruptedException {
		float[] points = { 0.9f, 0.7f, 0.5f, 0.3f, 0.1f };
		Progress progress = new Progress(m_logger, "reduced", points);
		int count = map.size();
		int index = 0;

		for (Map.Entry<Object, List<Object>> e : map.entrySet()) {
			joblet.reduce(context, e.getKey(), e.getValue());
			index++;

			progress.tick(index * 1.0f / count);
		}

		long duration = progress.getDuration();
		String rate = duration == 0 ? "N/A" : Formats.forNumber().format(count * 1000.0 / duration, "0.#", " Rows/s");

		progress.finish("reduced", rate);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

		// do map for each input files
		for (InputSplit split : splits) {
			MessageTreeReader reader = inputFormat.createRecordReader(split, context);

			reader.initialize(split, context);
			doMap(joblet, reader, mapperContext);
		}

		JobletMeta meta = joblet.getClass().getAnnotation(JobletMeta.class);
		final Map<Object, List<Object>> map = mapperContext.getMap();

		// do combine if have
		if (meta.combine()) {
			CombinerContext combinerContext = new CombinerContext(map);

			doReduce(joblet, map, combinerContext);
		}

		ReducerContext reducerContext = new ReducerContext(writer);

		// do reduce work
		doReduce(joblet, map, reducerContext);

		// show summary if have
		joblet.summary();

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

	static class Progress {
		private Logger m_logger;

		private String m_token;

		private float[] m_points;

		private long m_start;

		private long m_last;

		private float m_lastPoint;

		public Progress(Logger logger, String token, float[] points) {
			m_logger = logger;
			m_token = token;
			m_start = System.currentTimeMillis();
			m_last = m_start;
			m_lastPoint = 0;
			if(points==null){
				m_points = new float[0];
			}else{
				m_points = Arrays.copyOf(points, points.length);
			}
			m_logger.info(String.format("%s%% %s ...", 0, m_token));
		}

		public void finish(String token, String rate) {
			m_logger.info(String.format("%s%% %s in %s ms at %s.", 100, token, getDuration(), rate));
		}

		public long getDuration() {
			long end = System.currentTimeMillis();

			return end - m_start;
		}

		public void tick(float progress) {
			long now = System.currentTimeMillis();

			synchronized (this) {
				if (now - m_last > 1000) {
					int len = m_points.length;

					for (int i = len - 1; i >= 0; i--) {
						float point = m_points[i];

						if (progress >= point && m_lastPoint < point) {
							m_lastPoint = point;
							m_logger.info(String.format("%s%% %s ...", (int) (point * 100), m_token));
						}
					}
				}
			}
		}
	}

	static class CombinerContext extends AbstractJobletContext {
		private Map<Object, List<Object>> m_map;

		public CombinerContext(Map<Object, List<Object>> map) {
			m_map = map;
		}

		@Override
		public void write(Object key, Object value) throws IOException, InterruptedException {
			m_map.put(key, Arrays.asList(value));
		}

		@Override
		public boolean isInCombiner() {
			return true;
		}
	}

	static class ReducerContext extends AbstractJobletContext {
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
}
