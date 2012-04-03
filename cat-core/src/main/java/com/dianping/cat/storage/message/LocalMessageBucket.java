package com.dianping.cat.storage.message;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.site.lookup.annotation.Inject;

public class LocalMessageBucket implements Bucket<MessageTree> {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private String m_baseDir = "target/bucket";

	private ReentrantLock m_writeLock;

	private OutputStream m_writeDataFile;

	private String m_logicalPath;

	@Override
	public void close() throws IOException {
		m_writeLock.lock();

		try {
			m_writeDataFile.close();
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public MessageTree findById(String id) throws IOException {
		throw new UnsupportedOperationException("Not supported by local message bucket!");
	}

	@Override
	public MessageTree findNextById(String id, String tag) throws IOException {
		throw new UnsupportedOperationException("Not supported by local message bucket!");
	}

	@Override
	public MessageTree findPreviousById(String id, String tag) throws IOException {
		throw new UnsupportedOperationException("Not supported by local message bucket!");
	}

	@Override
	public void flush() throws IOException {
		m_writeLock.lock();

		try {
			m_writeDataFile.flush();
		} finally {
			m_writeLock.lock();
		}
	}

	@Override
	public Collection<String> getIds() {
		throw new UnsupportedOperationException("Not supported by local logview bucket!");
	}

	public String getLogicalPath() {
		return m_logicalPath;
	}

	@Override
	public void initialize(Class<?> type, String domain, Date timestamp) throws IOException {
		m_writeLock = new ReentrantLock();

		String logicalPath = m_pathBuilder.getMessagePath(domain, timestamp);
		File dataFile = new File(m_baseDir, logicalPath);

		dataFile.getParentFile().mkdirs();

		m_logicalPath = logicalPath;
		m_writeDataFile = new BufferedOutputStream(new FileOutputStream(dataFile, true), 8192);
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	@Override
	public boolean storeById(String id, MessageTree tree) throws IOException {
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

			return true;
		} finally {
			m_writeLock.unlock();
		}
	}
}
