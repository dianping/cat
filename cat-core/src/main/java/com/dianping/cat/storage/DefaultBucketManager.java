package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.spi.MessagePathBuilder;

public class DefaultBucketManager extends ContainerHolder implements BucketManager {
	private Map<Entry, Bucket<?>> m_map = new HashMap<Entry, Bucket<?>>();

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Override
	public void closeBucket(Bucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		}

		Entry key = null;

		synchronized (m_map) {
			for (Map.Entry<Entry, Bucket<?>> e : m_map.entrySet()) {
				if (e.getValue() == bucket) {
					key = e.getKey();
					break;
				}
			}
		}
		if (key != null) {
			m_map.remove(key);
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

	@SuppressWarnings("unchecked")
	protected <T> Bucket<T> getBucket(Class<T> type, long timestamp, String name, String namespace) throws IOException {
		String path;
		Date date = new Date(timestamp);

//		if (type == MessageTree.class) {
//			path = m_pathBuilder.getMessagePath(name, date);
//		} else {
//		}
		path = m_pathBuilder.getReportPath(name, date);

		Entry entry = new Entry(type, path, namespace);
		Bucket<?> bucket = m_map.get(entry);

		if (bucket == null) {
			synchronized (m_map) {
				bucket = m_map.get(entry);

				if (bucket == null) {
					bucket = createBucket(type, date, name, namespace);
					m_map.put(entry, bucket);
				}
			}
		}

		return (Bucket<T>) bucket;
	}

	@Override
	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

	static class Entry {
		private String m_namespace;

		private String m_path;

		private Class<?> m_type;

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
