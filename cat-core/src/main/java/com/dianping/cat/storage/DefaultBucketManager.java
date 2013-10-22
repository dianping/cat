package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Date;

import org.unidal.lookup.ContainerHolder;

public class DefaultBucketManager extends ContainerHolder implements BucketManager {

	@Override
	public void closeBucket(Bucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
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
		Date date = new Date(timestamp);
		Bucket<?> bucket = createBucket(type, date, name, namespace);

		return (Bucket<T>) bucket;
	}

	@Override
	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

	public static class Entry {
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
