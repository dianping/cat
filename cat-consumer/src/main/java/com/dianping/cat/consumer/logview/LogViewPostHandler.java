package com.dianping.cat.consumer.logview;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.internal.DefaultBucket;
import com.site.lookup.annotation.Inject;

public class LogViewPostHandler implements MessageAnalyzer {
	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private BucketManager m_bucketManager;

	private long m_startTime;

	private Set<String> m_domains;

	@Override
	public void analyze(MessageQueue queue) {
		throw new UnsupportedOperationException("This method should not be called!");
	}

	@Override
	public void doCheckpoint() throws IOException {
		for (String domain : m_domains) {
			String path = m_pathBuilder.getMessagePath(domain, new Date(m_startTime));

			try {
				DefaultBucket<byte[]> localBucket = (DefaultBucket<byte[]>) m_bucketManager.getBytesBucket(path);
				Bucket<byte[]> hdfsBucket = m_bucketManager.getHdfsBucket(path);

				hdfsBucket.deleteAndCreate();

				for (String id : localBucket.getIds()) {
					byte[] data = localBucket.findById(id);
					String[] tags = localBucket.findTagsById(id);

					hdfsBucket.storeById(id, data, tags);
				}

				localBucket.close();
				hdfsBucket.flush();
				hdfsBucket.close();
			} catch (IOException e) {
				throw new RuntimeException(String.format(
				      "Error when copying data from local bucket to HDFS bucket for %s.", path), e);
			}
		}
	}

	public void initialize(long startTime) {
		m_startTime = startTime;
	}

	public void setDomains(Set<String> domains) {
		m_domains = domains;
	}
}
