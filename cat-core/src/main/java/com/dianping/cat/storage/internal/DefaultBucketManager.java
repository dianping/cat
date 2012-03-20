package com.dianping.cat.storage.internal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ContainerHolder;

public class DefaultBucketManager extends ContainerHolder implements BucketManager, Disposable {
	private Map<Entry, Bucket<?>> m_map = new HashMap<Entry, Bucket<?>>();

	@Override
	public void closeBucket(Bucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		}

		release(bucket);
	}

	protected Bucket<?> createBucket(Class<?> type, Date timestamp, String name) throws IOException {
		Bucket<?> bucket = lookup(Bucket.class, type.getName());

		bucket.initialize(type, name, timestamp);
		return bucket;
	}

	@Override
	public void dispose() {
		for (Bucket<?> bucket : m_map.values()) {
			release(bucket);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> Bucket<T> getBucket(Class<T> type, Date timestamp, String name) throws IOException {
		Entry entry = new Entry(type, timestamp, name);
		Bucket<?> bucket = m_map.get(entry);

		if (bucket == null) {
			synchronized (this) {
				bucket = m_map.get(entry);

				if (bucket == null) {
					bucket = createBucket(type, timestamp, name);
					m_map.put(entry, bucket);
				}
			}
		}

		return (Bucket<T>) bucket;
	}

	@Override
	public Bucket<MessageTree> getMessageBucket(Date timestamp, String domain) throws IOException {
		return getBucket(MessageTree.class, timestamp, domain);
	}

	@Override
	public Bucket<String> getReportBucket(Date timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name);
	}

	static class Entry {
		private Class<?> m_type;

		private Date m_timestamp;

		private String m_name;

		public Entry(Class<?> type, Date timestamp, String name) {
			m_type = type;
			m_timestamp = timestamp;
			m_name = name;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry e = (Entry) obj;

				return e.getType() == m_type && e.getTimestamp().getTime() == m_timestamp.getTime()
				      && e.getName().equals(m_name);
			}

			return false;
		}

		public String getName() {
			return m_name;
		}

		public Date getTimestamp() {
			return m_timestamp;
		}

		public Class<?> getType() {
			return m_type;
		}

		@Override
		public int hashCode() {
			int hashcode = m_type.hashCode();

			hashcode = hashcode * 31 + m_name.hashCode();
			return hashcode;
		}
	}
}
