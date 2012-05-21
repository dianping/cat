package com.dianping.cat.job.spi.joblet;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import com.dianping.cat.job.JobApp;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;

public class JobletMapper extends Mapper<Object, MessageTreeWritable, Object, Object> {
	private Joblet<Object, Object> m_joblet;

	private JobletContext m_context;

	@Override
	protected void cleanup(JobletMapper.Context context) throws IOException, InterruptedException {
		System.out.println("Mapper done.");
		JobApp.release(m_joblet);
	}

	@Override
	protected void map(Object key, MessageTreeWritable treeWritable, final JobletMapper.Context context)
	      throws IOException, InterruptedException {
		m_joblet.map(m_context, treeWritable);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void setup(final JobletMapper.Context context) throws IOException, InterruptedException {
		System.out.println("Mapper starting ...");
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
			public void write(Object key, Object value) throws IOException, InterruptedException {
				context.write(key, value);
			}
		};
		
		// if a reducer is run during mapper phase, it must be a combiner
		context.getConfiguration().setBoolean("joblet.combiner", true);
	}
}
