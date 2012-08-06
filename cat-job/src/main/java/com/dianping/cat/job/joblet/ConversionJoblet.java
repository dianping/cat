package com.dianping.cat.job.joblet;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;

import com.dianping.cat.job.spi.JobCmdLine;
import com.dianping.cat.job.spi.joblet.Joblet;
import com.dianping.cat.job.spi.joblet.JobletContext;
import com.dianping.cat.job.spi.joblet.JobletMeta;
import com.dianping.cat.job.spi.mapreduce.MessageTreeWritable;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.dump.LocalMessageBucket;
import com.dianping.cat.storage.dump.MessageBucket;
import com.site.lookup.ContainerHolder;

@JobletMeta(name = "conversion", description = "File Conversion", keyClass = IntWritable.class, valueClass = IntWritable.class, reducerNum = 1)
public class ConversionJoblet extends ContainerHolder implements Joblet<IntWritable, IntWritable> {
	private LocalMessageBucket m_bucket;

	private MessageIdFactory m_factory;

	@Override
	public boolean initialize(JobCmdLine cmdLine) {
		String inputPath = cmdLine.getArg("inputPath", 0, null);
		String outputPath = cmdLine.getArg("outputPath", 1, null);

		if (inputPath != null) {
			cmdLine.setProperty("inputPath", inputPath);
		}

		if (outputPath != null) {
			cmdLine.setProperty("outputPath", outputPath);
		}

		return true;
	}

	@Override
	public void map(JobletContext context, MessageTreeWritable treeWritable) throws IOException, InterruptedException {
		MessageTree tree = treeWritable.get();

		if (m_factory == null) {
			m_factory = new MockMessageIdFactory();
			m_factory.setIpAddress("7f000001");
			m_factory.initialize("Test");
		}

		if (m_bucket == null) {
			m_bucket = (LocalMessageBucket) lookup(MessageBucket.class, LocalMessageBucket.ID);
			m_bucket.setBaseDir(new File("/Users/qmwu/dump2"));
			m_bucket.initialize("GroupService-10.1.6.108");
		}

		try {
			tree.setMessageId(m_factory.getNextId());
			 m_bucket.store(tree);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void reduce(JobletContext context, IntWritable key, Iterable<IntWritable> stats) throws IOException {
	}

	@Override
	public void summary() {
	}

	static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		protected long getTimestamp() {
			return 1343532130488L;
		}
	}
}
