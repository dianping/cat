package com.dianping.cat.job.browser;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.Writable;

import com.dianping.cat.hadoop.mapreduce.MessageTreeWritable;
import com.dianping.cat.job.JobCmdLine;
import com.dianping.cat.joblet.Joblet;
import com.dianping.cat.joblet.JobletContext;
import com.dianping.cat.joblet.JobletMeta;
import com.site.lookup.ContainerHolder;

@JobletMeta(name = "help", description = "Print this help", keyClass = Writable.class, valueClass = Writable.class)
public class HelpJoblet extends ContainerHolder implements Joblet<Writable, Writable> {
	@Override
	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reduce(JobletContext context, Writable key, Iterable<Writable> values) throws IOException,
	      InterruptedException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean initialize(JobCmdLine cmdLine) {
		List<Joblet> joblets = super.lookupList(Joblet.class);

		System.out.println("Syntax: hadoop jar <jar-file> <joblet>");
		System.out.println("available joblets:");

		for (Joblet joblet : joblets) {
			JobletMeta meta = joblet.getClass().getAnnotation(JobletMeta.class);

			System.out.println(String.format("   %s: %s%s", meta.name(), space(15 - meta.name().length()),
			      meta.description()));
		}

		return false;
	}

	private String space(int count) {
		StringBuilder sb = new StringBuilder(count);

		for (int i = 0; i < count; i++) {
			sb.append(' ');
		}

		return sb.toString();
	}
}
