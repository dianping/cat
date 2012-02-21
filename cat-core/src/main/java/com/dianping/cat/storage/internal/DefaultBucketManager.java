package com.dianping.cat.storage.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.MessageBucket;
import com.site.lookup.ContainerHolder;

public class DefaultBucketManager extends ContainerHolder implements BucketManager, Disposable {
	private Map<Entry, Bucket<?>> m_map = new HashMap<Entry, Bucket<?>>();

	protected Bucket<?> createBucket(String path, Class<?> type) throws IOException {
		Bucket<?> bucket = lookup(Bucket.class, type.getName());

		bucket.initialize(type, path);
		return bucket;
	}

	@Override
	public void dispose() {
		for (Bucket<?> bucket : m_map.values()) {
			release(bucket);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Bucket<T> getBucket(Class<T> type, String path) throws IOException {
		if (type == null || path == null) {
			throw new IllegalArgumentException(String.format("Type(%s) or path(%s) can't be null.", type, path));
		}

		Entry entry = new Entry(type, path);
		Bucket<?> bucket = m_map.get(entry);

		if (bucket == null) {
			synchronized (this) {
				bucket = m_map.get(entry);

				if (bucket == null) {
					bucket = createBucket(path, type);
					m_map.put(entry, bucket);
				}
			}
		}

		return (Bucket<T>) bucket;
	}

	@Override
	public MessageBucket getMessageBucket(String path) throws IOException {
		return (MessageBucket) getBucket(MessageTree.class, path);
	}

	static class Entry {
		private Class<?> m_type;

		private String m_path;

		public Entry(Class<?> type, String path) {
			m_type = type;
			m_path = path;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry e = (Entry) obj;

				return e.getClass() == m_type && e.getPath().equals(m_path);
			}

			return false;
		}

		public String getPath() {
			return m_path;
		}

		public Class<?> getType() {
			return m_type;
		}

		@Override
		public int hashCode() {
			int hashcode = m_type.hashCode();

			hashcode = hashcode * 31 + m_path.hashCode();
			return hashcode;
		}
	}
}
