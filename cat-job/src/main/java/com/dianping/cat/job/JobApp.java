package com.dianping.cat.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.codehaus.plexus.PlexusContainer;

import com.site.lookup.ContainerLoader;
import com.site.lookup.LookupException;

public class JobApp extends Configured implements Tool {
	private PlexusContainer m_container;

	private JobApp() {
		m_container = ContainerLoader.getDefaultContainer();
	}

	protected <T> T lookup(Class<T> role) throws LookupException {
		return lookup(role, null);
	}

	@SuppressWarnings("unchecked")
	protected <T> T lookup(Class<T> role, String roleHint) throws LookupException {
		try {
			return (T) m_container.lookup(role, roleHint == null ? "default" : roleHint.toString());
		} catch (Exception e) {
			throw new LookupException("Component(" + role.getName() + (roleHint == null ? "default" : roleHint.toString())
			      + ") lookup failure. details: " + e.getMessage(), e);
		}
	}

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), new JobApp(), args);

		System.exit(exitCode);
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = JobFactory.INSTANCE.createJob(this, getConf(), new JobCmdLine(args));

		if (job == null) {
			return -2;
		} else if (job.waitForCompletion(true)) {
			return 0;
		} else {
			return -1;
		}
	}
}
