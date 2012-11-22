package com.dianping.cat.job;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;

import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.JobFactory;
import com.dianping.cat.job.spi.joblet.Joblet;
import org.unidal.lookup.ContainerLoader;

public class JobApp extends Configured implements Tool {
	private static final JobApp s_instance = new JobApp();

	private PlexusContainer m_container = ContainerLoader.getDefaultContainer();

	public static void main(String[] args) throws Exception {
		int exitCode = ToolRunner.run(new Configuration(), s_instance, args);

		System.exit(exitCode);
	}

	public static <T> T lookup(Class<T> role) {
		return lookup(role, null);
	}

	public static <T> T lookup(Class<T> role, String roleHint) {
		try {
			return (T) s_instance.m_container.lookup(role, roleHint == null ? "default" : roleHint.toString());
		} catch (Exception e) {
			throw new org.unidal.lookup.LookupException("Component(" + role.getName() + ":"
			      + (roleHint == null ? "default" : roleHint.toString()) + ") lookup failure. details: " + e.getMessage(),
			      e);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		JobCmdLine cmdLine = new JobCmdLine(args);
		String name = cmdLine.getJobletName();
		Joblet<?, ?> joblet = lookup(Joblet.class, name);
		Job job = JobFactory.INSTANCE.createJob(joblet, getConf(), cmdLine);

		if (job == null) {
			return -1;
		} else {
			long start = System.currentTimeMillis();

			try {
				if (job.waitForCompletion(true)) {
					return 0;
				} else {
					return -2;
				}
			} finally {
				long end = System.currentTimeMillis();

				System.out.println(String.format("Time used: %s ms", end - start));
			}
		}
	}

	public static void release(Object component) {
		if (component != null) {
			try {
				s_instance.m_container.release(component);
			} catch (ComponentLifecycleException e) {
				e.printStackTrace();
			}
		}
	}
}
