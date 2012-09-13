package com.dianping.cat.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.message.LocalLogviewBucket;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultBucketManager extends ContainerHolder implements BucketManager {
	private Map<Entry, Bucket<?>> m_map = new HashMap<Entry, Bucket<?>>();

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Override
	public void closeAllLogviewBuckets() {
		int hour = 60 * 60 * 1000;
		long currentTimeMillis = System.currentTimeMillis();
		long lastHour = currentTimeMillis - currentTimeMillis % hour - 2 * hour;

		List<Entry> removeEntries = new ArrayList<Entry>();
		for (java.util.Map.Entry<Entry, Bucket<?>> temp : m_map.entrySet()) {
			Entry entry = temp.getKey();
			Bucket<?> bucket = temp.getValue();

			if (bucket instanceof LocalLogviewBucket) {
				long timestamp = ((LocalLogviewBucket) bucket).getTimestamp();

				if (timestamp < lastHour) {
					removeEntries.add(entry);
				}
			}
		}

		synchronized (m_map) {
			for (Entry entry : removeEntries) {
				Bucket<?> bucket = m_map.get(entry);
				try {
					bucket.close();
				} catch (Exception e) {
					// ignore it
				}
				m_map.remove(entry);
				release(bucket);
			}
		}
	}

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

		if (type == MessageTree.class) {
			path = m_pathBuilder.getMessagePath(name, date);
		} else {
			path = m_pathBuilder.getReportPath(name, date);
		}

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
	public Bucket<MessageTree> getLogviewBucket(long timestamp, String domain) throws IOException {
		return getBucket(MessageTree.class, timestamp, domain, "logview");
	}

	@Override
	public List<Bucket<MessageTree>> getLogviewBuckets(long timestamp, String excludeDomain) throws IOException {
		long t = timestamp - timestamp % (60 * 60 * 1000L);
		List<Bucket<MessageTree>> buckets = new ArrayList<Bucket<MessageTree>>();

		for (Bucket<?> bucket : m_map.values()) {
			if (bucket instanceof LocalLogviewBucket) {
				LocalLogviewBucket logview = (LocalLogviewBucket) bucket;

				if (logview.getTimestamp() == t && !logview.getDomain().equals(excludeDomain)) {
					buckets.add(logview);
				}
			}
		}
		return buckets;
	}

	@Override
	public Bucket<MessageTree> getMessageBucket(long timestamp, String domain) throws IOException {
		return getBucket(MessageTree.class, timestamp, domain, "message");
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
