package com.dianping.cat.storage.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.TagThreadSupport;
import com.site.helper.Joiners;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public abstract class AbstractFileBucket<T> implements Bucket<T>, TagThreadSupport<T>, LogEnabled {
	private static final String[] EMPTY = new String[0];

	@Inject
	private String m_baseDir;

	// key => offset of record
	private Map<String, Long> m_idToOffsets = new HashMap<String, Long>();

	// tag => list of ids
	private Map<String, List<String>> m_tagToIds = new HashMap<String, List<String>>();

	private File m_file;

	private RandomAccessFile m_out;

	private Logger m_logger;

	private ReadLock m_readLock;

	private WriteLock m_writeLock;

	@Override
	public void close() {
		m_writeLock.lock();

		try {
			m_out.close();
			m_idToOffsets.clear();
			m_tagToIds.clear();
		} catch (IOException e) {
			// ignore it
		} finally {
			m_writeLock.unlock();
		}
	}

	protected abstract T decode(ChannelBuffer buf) throws IOException;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	protected abstract void encode(T data, ChannelBuffer buf) throws IOException;

	@Override
	public List<T> findAllByIds(List<String> ids) {
		List<T> list = new ArrayList<T>(ids.size());

		for (String id : ids) {
			list.add(findById(id));
		}

		return list;
	}

	@Override
	public List<String> findAllIdsByTag(String tag) {
		List<String> ids = m_tagToIds.get(tag);

		if (ids == null) {
			return Collections.emptyList();
		} else {
			return ids;
		}
	}

	@Override
	public T findById(String id) {
		Long offset = m_idToOffsets.get(id);

		if (offset != null) {
			m_readLock.lock();

			try {
				long old = m_out.getFilePointer();

				m_out.seek(offset);
				m_out.readLine(); // first line is header, get rid of it

				int num = Integer.parseInt(m_out.readLine());
				byte[] bytes = new byte[num];

				m_out.readFully(bytes);

				ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);

				T data = decode(buf);
				m_out.seek(old);

				return data;
			} catch (Exception e) {
				m_logger.error(String.format("Error when reading file(%s)!", m_file), e);
			} finally {
				m_readLock.unlock();
			}
		}

		return null;
	}

	@Override
	public T findNextById(String id, Direction direction, String tag) {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			switch (direction) {
			case FORWARD:
				index++;
				break;
			case BACKWARD:
				index--;
				break;
			}

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public void initialize(Class<?> type, String path) throws IOException {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

		m_readLock = lock.readLock();
		m_writeLock = lock.writeLock();
		m_file = new File(m_baseDir, path);
		m_file.getParentFile().mkdirs();
		m_out = new RandomAccessFile(m_file, "rw");

		if (m_file.exists()) {
			loadIndexes();
		}
	}

	protected void loadIndexes() throws IOException {
		byte[] data = new byte[8192];

		m_writeLock.lock();

		try {
			while (true) {
				long offset = m_out.getFilePointer();
				String first = m_out.readLine();

				if (first == null) { // EOF
					break;
				}

				int num = Integer.parseInt(m_out.readLine());

				if (num > data.length) {
					int newSize = data.length;

					while (newSize < num) {
						newSize += newSize / 2;
					}

					data = new byte[newSize];
				}

				m_out.readFully(data, 0, num); // get rid of it
				m_out.readLine(); // get rid of empty line

				List<String> parts = Splitters.by('\t').split(first);
				if (parts.size() > 0) {
					String id = parts.get(0);

					parts.remove(0);
					updateIndex(id, parts.toArray(EMPTY), offset);
				}
			}
		} finally {
			m_writeLock.unlock();
		}
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	@Override
	public boolean storeById(String id, T data) {
		return storeById(id, data, EMPTY);
	}

	/**
	 * Store the message in the format of:<br>
	 * 
	 * <xmp> <id>\t<tag1>\t<tag2>\t...\n <length of message>\n <message>\n </xmp>
	 */
	@Override
	public boolean storeById(String id, T data, String... tags) {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		String attributes = id + "\t" + Joiners.by('\t').join(tags) + "\n";
		byte[] first;
		byte[] num;
		int length;

		try {
			encode(data, buf);

			length = buf.readInt();
			first = attributes.getBytes("utf-8");
			num = String.valueOf(length).getBytes("utf-8");
		} catch (Exception e) {
			m_logger.error(String.format("Error when preparing to write to file(%s)!", m_file), e);

			return false;
		}

		m_writeLock.lock();

		try {
			long offset = m_out.getFilePointer();

			m_out.write(first);
			m_out.write(num);
			m_out.write('\n');
			m_out.write(buf.array(), buf.readerIndex(), length);
			m_out.write('\n');
			m_out.getChannel().force(true);

			updateIndex(id, tags, offset);

			return true;
		} catch (Exception e) {
			m_logger.error(String.format("Error when writing to file(%s)!", m_file), e);

			return false;
		} finally {
			m_writeLock.unlock();
		}
	}

	protected void updateIndex(String id, String[] tags, long offset) {
		m_idToOffsets.put(id, offset);

		for (String tag : tags) {
			List<String> ids = m_tagToIds.get(tag);

			if (ids == null) {
				ids = new ArrayList<String>();
				m_tagToIds.put(tag, ids);
			}

			ids.add(id);
		}
	}
}
