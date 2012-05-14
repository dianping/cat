package com.dianping.cat.job.spi.joblet;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

public class JobletReducer extends Reducer<Object, Object, Object, Object> {
	private Joblet<Object, Object> m_joblet;

	@Override
	protected void reduce(Object key, Iterable<Object> values, final JobletReducer.Context context) throws IOException,
	      InterruptedException {
		m_joblet.reduce(new JobletContext() {
			@Override
			public void write(Object key, Object value) throws IOException, InterruptedException {
				context.write(key, value);
			}
		}, key, values);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setup(Context context) throws IOException, InterruptedException {
		Configuration configuration = context.getConfiguration();

		Class<? extends Joblet> jobletClass = configuration.getClass("joblet.class", null, Joblet.class);

		if (jobletClass == null) {
			throw new RuntimeException("Internal error: no joblet class passed to mapper!");
		}

		try {
			m_joblet = jobletClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when getting instance of %s!", jobletClass));
		}
	}
}
