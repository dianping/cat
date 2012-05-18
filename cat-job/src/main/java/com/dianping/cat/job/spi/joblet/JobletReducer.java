package com.dianping.cat.job.spi.joblet;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

import com.dianping.cat.job.JobApp;

public class JobletReducer extends Reducer<Object, Object, Object, Object> {
	private Joblet<Object, Object> m_joblet;

	private AbstractJobletContext m_context;

	@Override
	protected void cleanup(JobletReducer.Context context) throws IOException, InterruptedException {
		JobApp.release(m_joblet);
		System.out.println("Reducer done.");

		m_joblet.summary();
	}

	@Override
	protected void reduce(Object key, Iterable<Object> values, final JobletReducer.Context context) throws IOException,
	      InterruptedException {
		m_joblet.reduce(m_context, key, values);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void setup(final JobletReducer.Context context) throws IOException, InterruptedException {
		System.out.println("Reducer starting ...");

		Configuration configuration = context.getConfiguration();
		String jobletName = configuration.get("joblet.name");

		if (jobletName == null) {
			throw new RuntimeException("Internal error: no joblet name passed to mapper!");
		}

		try {
			m_joblet = JobApp.lookup(Joblet.class, jobletName);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when doing lookup Joblet(%s)!", jobletName));
		}

		m_context = new AbstractJobletContext() {
			@Override
			public boolean isInCombiner() {
				return context.getConfiguration().getBoolean("joblet.combiner", false);
			}

			@Override
			public void write(Object key, Object value) throws IOException, InterruptedException {
				context.write(key, value);
			}
		};
	}
}
