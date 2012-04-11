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

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.site.helper.Splitters;
import com.site.helper.Splitters.StringSplitter;
import com.site.lookup.annotation.Inject;

public class LocalLogviewBucket implements Bucket<MessageTree>, LogEnabled {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private ServerConfigManager m_configManager;

	private String m_baseDir = "target/bucket/logview";

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
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

	public Meta getMeta(String id) {
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

				return new Meta(data.getMessageId(), data.getThreadId(), offset, num);
			} catch (Exception e) {
				m_logger.error(String.format("Error when reading file(%s)!", m_readDataFile), e);
			} finally {
				m_readLock.unlock();
			}
		}

		return null;
	}

	public static class Meta {
		private String m_messageId;

		private String m_tagThread;

		private long m_offset;

		private int m_legnth;

		public Meta(String messageId, String tagThread, long offset, int length) {
			m_messageId = messageId;
			m_tagThread = tagThread;
			m_offset = offset;
			m_legnth = length;
		}

		public String getMessageId() {
			return m_messageId;
		}

		public String getTagThread() {
			return m_tagThread;
		}

		public long getOffset() {
			return m_offset;
		}

		public int getLegnth() {
			return m_legnth;
		}
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
	public Collection<String> getIds() {
		return m_idToOffsets.keySet();
	}

	public String getLogicalPath() {
		return m_logicalPath;
	}

	@Override
	public void initialize(Class<?> type, String domain, Date timestamp) throws IOException {
		ServerConfig serverConfig = m_configManager.getServerConfig();

		if (serverConfig != null) {
			m_baseDir = serverConfig.getStorage().getLocalBaseDir();
		}

		m_writeLock = new ReentrantLock();
		m_readLock = new ReentrantLock();

		String logicalPath = m_pathBuilder.getMessagePath(domain, timestamp);

		File dataFile = new File(m_baseDir, logicalPath);
		File indexFile = new File(m_baseDir, logicalPath + ".idx");

		if (indexFile.exists()) {
			loadIndexes(indexFile);
		}

		dataFile.getParentFile().mkdirs();

		m_logicalPath = logicalPath;
		m_writeDataFile = new BufferedOutputStream(new FileOutputStream(dataFile, true), 8192);
		m_writeIndexFile = new BufferedOutputStream(new FileOutputStream(indexFile, true), 8192);
		m_writeDataFileLength = dataFile.length();
		m_readDataFile = new RandomAccessFile(dataFile, "r");
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

				if (parts.size() >= 3) {
					String id = parts.get(0);
					String offset = parts.get(1);
					String tag = parts.get(2);

					try {
						updateIndex(id, Long.parseLong(offset), tag);
					} catch (NumberFormatException e) {
						// ignore it
					}
				}
			}
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public boolean storeById(String id, MessageTree tree) throws IOException {
		if (m_idToOffsets.containsKey(id)) {
			return false;
		}

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
			String tag = "t:" + tree.getThreadId();
			String line = id + '\t' + offset + '\t' + tag + '\n';
			byte[] data = line.getBytes("utf-8");

			m_writeDataFileLength += num.length + 1 + length + 1;
			m_writeIndexFile.write(data);
			m_dirty.set(true);

			updateIndex(id, offset, tag);

			return true;
		} finally {
			m_writeLock.unlock();
		}
	}

	protected void updateIndex(String id, long offset, String tag) {
		m_idToOffsets.put(id, offset);

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
