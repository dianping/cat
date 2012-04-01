package com.dianping.cat.storage.internal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultBucketManager extends ContainerHolder implements BucketManager, Disposable {
	@Inject
	private MessagePathBuilder m_pathBuilder;

	private Map<Entry, Bucket<?>> m_map = new HashMap<Entry, Bucket<?>>();

	@Override
	public void closeBucket(Bucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		}

		synchronized (m_map) {
			for (Map.Entry<Entry, Bucket<?>> e : m_map.entrySet()) {
				if (e.getValue() == bucket) {
					m_map.remove(e.getKey());
					break;
				}
			}
		}

		release(bucket);
	}

	protected Bucket<?> createBucket(Class<?> type, Date timestamp, String name, String namespace) throws IOException {
		try {
			Bucket<?> bucket = lookup(Bucket.class, type.getName() + "-" + namespace);

			bucket.initialize(type, name, timestamp);
			return bucket;
		} catch (RuntimeException e) {
			e.printStackTrace();

			throw e;
		}
	}

	@Override
	public void dispose() {
		synchronized (m_map) {
			for (Bucket<?> bucket : m_map.values()) {
				release(bucket);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> Bucket<T> getBucket(Class<T> type, Date timestamp, String name, String namespace) throws IOException {
		String path;

		if (type == MessageTree.class) {
			path = m_pathBuilder.getMessagePath(name, timestamp);
		} else {
			path = m_pathBuilder.getReportPath(name, timestamp);
		}

		Entry entry = new Entry(type, path, namespace);
		Bucket<?> bucket = m_map.get(entry);

		if (bucket == null) {
			synchronized (m_map) {
				bucket = m_map.get(entry);

				if (bucket == null) {
					bucket = createBucket(type, timestamp, name, namespace);
					m_map.put(entry, bucket);
				}
			}
		}

		return (Bucket<T>) bucket;
	}

	@Override
	public Bucket<MessageTree> getLogviewBucket(Date timestamp, String domain) throws IOException {
		return getBucket(MessageTree.class, timestamp, domain, "logview");
	}

	@Override
	public Bucket<MessageTree> getMessageBucket(Date timestamp, String domain) throws IOException {
		return getBucket(MessageTree.class, timestamp, domain, "message");
	}

	@Override
	public Bucket<String> getReportBucket(Date timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

	static class Entry {
		private Class<?> m_type;

		private String m_path;

		private String m_namespace;

		public Entry(Class<?> type, String path, String namespace) {
			m_type = type;
			m_path = path;
			m_namespace = namespace;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry e = (Entry) obj;

				return e.m_type == m_type && e.m_path.equals(m_path) && e.m_namespace.equals(m_namespace);
			}

			return false;
		}

		@Override
		public int hashCode() {
			int hashcode = m_type.hashCode();

			hashcode = hashcode * 31 + m_path.hashCode();
			return hashcode;
		}

		@Override
		public String toString() {
			return String.format("Entry[type=%s,path=%s]", m_type, m_path);
		}
	}
}
