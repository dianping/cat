package com.dianping.cat.storage.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.TagThreadSupport;
import com.site.helper.Joiners;
import com.site.helper.Splitters;

public abstract class AbstractFileBucket<T> implements Bucket<T>, TagThreadSupport<T>, LogEnabled {
	private static final String[] EMPTY = new String[0];

	// key => offset of record
	private Map<String, Long> m_idToOffsets = new HashMap<String, Long>();

	// tag => list of ids
	private Map<String, List<String>> m_tagToIds = new HashMap<String, List<String>>();

	private File m_file;

	private RandomAccessFile m_readFile;

	private RandomAccessFile m_writeFile;

	private ReentrantLock m_readLock;

	private ReentrantLock m_writeLock;

	private Logger m_logger;

	@Override
	public void close() {
		m_writeLock.lock();

		try {
			m_idToOffsets.clear();
			m_tagToIds.clear();
			m_writeFile.close();
		} catch (IOException e) {
			// ignore it
		} finally {
			m_writeLock.unlock();
		}
	}

	protected abstract T decode(ChannelBuffer buf) throws IOException;

	@Override
	public void deleteAndCreate() {
		m_writeLock.lock();
		m_readLock.lock();

		m_idToOffsets.clear();
		m_tagToIds.clear();

		try {
			m_file.delete();
			m_writeFile = new RandomAccessFile(m_file, "rw");
			m_readFile = new RandomAccessFile(m_file, "r");
		} catch (FileNotFoundException e) {
			m_logger.error(String.format("Error when clearing file bucket(%s)!", m_file), e);
		} finally {
			m_readLock.unlock();
			m_writeLock.unlock();
		}
	}

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
				long old = m_readFile.getFilePointer();

				m_readFile.seek(offset);
				m_readFile.readLine(); // first line is header, get rid of it

				int num = Integer.parseInt(m_readFile.readLine());
				byte[] bytes = new byte[num];

				m_readFile.readFully(bytes);

				ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);

				T data = decode(buf);
				m_readFile.seek(old);

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

	public Set<String> getIds() {
		return m_idToOffsets.keySet();
	}

	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		File path = new File(baseDir, logicalPath);
		m_writeLock = new ReentrantLock();
		m_readLock = new ReentrantLock();
		m_file = path;
		m_file.getParentFile().mkdirs();
		m_writeFile = new RandomAccessFile(m_file, "rw");
		m_readFile = new RandomAccessFile(m_file, "r");

		if (m_file.exists()) {
			loadIndexes();
		}
	}

	protected abstract boolean isAutoFlush();

	protected void loadIndexes() throws IOException {
		byte[] data = new byte[8192];

		m_writeLock.lock();

		try {
			while (true) {
				long offset = m_writeFile.getFilePointer();
				String first = m_writeFile.readLine();

				if (first == null) { // EOF
					break;
				}

				int num = -1;

				// if the index was corrupted, then try to skip some lines
				try {
					num = Integer.parseInt(m_writeFile.readLine());
				} catch (NumberFormatException e) {
					m_logger.warn("Error during loadIndexes: " + e.getMessage());
				}

				if (num > data.length) {
					int newSize = data.length;

					while (newSize < num) {
						newSize += newSize / 2;
					}

					data = new byte[newSize];
				}

				m_writeFile.readFully(data, 0, num); // get rid of it
				m_writeFile.readLine(); // get rid of empty line

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
		if (m_idToOffsets.containsKey(id)) {
			return false;
		}

		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		String attributes = id + "\t" + Joiners.by('\t').join(tags) + "\n";
		byte[] firstLine;
		byte[] num;
		int length;

		try {
			encode(data, buf);

			length = buf.readInt();
			firstLine = attributes.getBytes("utf-8");
			num = String.valueOf(length).getBytes("utf-8");
		} catch (Exception e) {
			m_logger.error(String.format("Error when preparing to write to file(%s)!", m_file), e);

			return false;
		}

		m_writeLock.lock();

		try {
			long offset = m_writeFile.getFilePointer();

			m_writeFile.write(firstLine);
			m_writeFile.write(num);
			m_writeFile.write('\n');
			m_writeFile.write(buf.array(), buf.readerIndex(), length);
			m_writeFile.write('\n');

			if (isAutoFlush()) {
				m_writeFile.getChannel().force(true);
			}

			updateIndex(id, tags, offset);

			return true;
		} catch (Exception e) {
			m_logger.error(String.format("Error when writing to file(%s)!", m_file), e);

			return false;
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public String toString() {
		return String.format("%s[file=%s, ids=%s]", getClass().getSimpleName(), m_file, m_idToOffsets.keySet());
	}

	protected void updateIndex(String id, String[] tags, long offset) {
		m_idToOffsets.put(id, offset);

		for (String tag : tags) {
			List<String> ids = m_tagToIds.get(tag);

			if (ids == null) {
				ids = new ArrayList<String>();
				m_tagToIds.put(tag, ids);
			}

			if (!ids.contains(id)) {
				ids.add(id);
			}
		}
	}
}
