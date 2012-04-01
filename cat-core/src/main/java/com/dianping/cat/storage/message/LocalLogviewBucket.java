package com.dianping.cat.storage.message;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.site.helper.Joiners;
import com.site.helper.Splitters;
import com.site.helper.Splitters.StringSplitter;
import com.site.lookup.annotation.Inject;

public class LocalLogviewBucket implements Bucket<MessageTree>, LogEnabled {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private String m_baseDir = "target/bucket";

	// key => offset of record
	private Map<String, Long> m_idToOffsets = new HashMap<String, Long>();

	// tag => list of ids
	private Map<String, List<String>> m_tagToIds = new HashMap<String, List<String>>();

	private ReentrantLock m_readLock;

	private RandomAccessFile m_readDataFile;

	private ReentrantLock m_writeLock;

	private long m_writeDataFileLength;

	private OutputStream m_writeDataFile;

	private OutputStream m_writeIndexFile;

	private AtomicBoolean m_dirty = new AtomicBoolean();

	private Logger m_logger;

	private String m_logicalPath;

	@Override
	public void close() throws IOException {
		m_writeLock.lock();

		try {
			m_idToOffsets.clear();
			m_tagToIds.clear();
			m_writeDataFile.close();
			m_writeIndexFile.close();
		} catch (Exception e) {
			// ignore it
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public void deleteAndCreate() throws IOException {
		new File(m_baseDir, m_logicalPath).delete();
		new File(m_baseDir, m_logicalPath + ".idx").delete();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<String> findAllById(String id) throws IOException {
		throw new UnsupportedOperationException("Not supported by local logview bucket!");
	}

	@Override
	public MessageTree findById(String id) throws IOException {
		Long offset = m_idToOffsets.get(id);

		if (offset != null) {
			m_readLock.lock();

			try {
				if (m_dirty.get()) {
					flush(); // flush first if any read requesting
				}

				m_readDataFile.seek(offset);

				int num = Integer.parseInt(m_readDataFile.readLine());
				byte[] bytes = new byte[num];

				m_readDataFile.readFully(bytes);

				ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);
				MessageTree data = m_codec.decode(buf);

				return data;
			} catch (Exception e) {
				m_logger.error(String.format("Error when reading file(%s)!", m_readDataFile), e);
			} finally {
				m_readLock.unlock();
			}
		}

		return null;
	}

	@Override
	public MessageTree findNextById(String id, String tag) throws IOException {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			index++;

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public MessageTree findPreviousById(String id, String tag) throws IOException {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			index--;

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public void flush() throws IOException {
		m_writeLock.lock();

		try {
			m_writeDataFile.flush();
			m_writeIndexFile.flush();
		} finally {
			m_dirty.set(false);
			m_writeLock.unlock();
		}
	}

	@Override
	public Collection<String> getIdsByPrefix(String tag) {
		throw new UnsupportedOperationException("Not supported by local logview bucket!");
	}

	@Override
	public void initialize(Class<?> type, String domain, Date timestamp) throws IOException {
		m_writeLock = new ReentrantLock();
		m_readLock = new ReentrantLock();

		String logicalPath = m_pathBuilder.getMessagePath(domain, timestamp);

		File dataFile = new File(m_baseDir, logicalPath);
		File indexFile = new File(m_baseDir, logicalPath + ".idx");

		dataFile.getParentFile().mkdirs();

		m_logicalPath = logicalPath;
		m_writeDataFile = new BufferedOutputStream(new FileOutputStream(dataFile, true), 8192);
		m_writeIndexFile = new BufferedOutputStream(new FileOutputStream(indexFile, true), 8192);
		m_readDataFile = new RandomAccessFile(dataFile, "r");

		if (indexFile.exists()) {
			loadIndexes(indexFile);
		}
	}

	protected void loadIndexes(File indexFile) throws IOException {
		m_writeLock.lock();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(indexFile));
			StringSplitter splitter = Splitters.by('\t');

			while (true) {
				String line = reader.readLine();

				if (line == null) { // EOF
					break;
				}

				List<String> parts = splitter.split(line);

				if (parts.size() >= 2) {
					String id = parts.remove(0);
					String offset = parts.remove(0);

					try {
						updateIndex(id, Long.parseLong(offset), parts);
					} catch (NumberFormatException e) {
						// ignore it
					}
				}
			}
		} finally {
			m_writeLock.unlock();
		}
	}

	protected List<String> prepareTags(MessageTree tree) {
		List<String> tags = new ArrayList<String>(1);

		tags.add("t:" + tree.getThreadId());

		return tags;
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	@Override
	public boolean storeById(String id, MessageTree tree) throws IOException {
		if (m_idToOffsets.containsKey(id)) {
			return false;
		}

		List<String> tags = prepareTags(tree);
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);

		int length = buf.readInt();
		byte[] num = String.valueOf(length).getBytes("utf-8");

		m_writeLock.lock();

		try {
			m_writeDataFile.write(num);
			m_writeDataFile.write('\n');
			m_writeDataFile.write(buf.array(), buf.readerIndex(), length);
			m_writeDataFile.write('\n');

			long offset = m_writeDataFileLength;
			String line = id + '\t' + offset + '\t' + Joiners.by('\t').join(tags) + '\n';
			byte[] data = line.getBytes("utf-8");

			m_writeDataFileLength += num.length + 1 + length + 1;
			m_writeIndexFile.write(data);
			m_dirty.set(true);

			updateIndex(id, offset, tags);

			return true;
		} finally {
			m_writeLock.unlock();
		}
	}

	protected void updateIndex(String id, long offset, List<String> tags) {
		m_idToOffsets.put(id, offset);

		for (String tag : tags) {
			List<String> ids = m_tagToIds.get(tag);

			if (ids == null) {
				ids = new ArrayList<String>(3);

				m_tagToIds.put(tag, ids);
			}

			if (!ids.contains(id)) {
				ids.add(id);
			}
		}
	}
}
