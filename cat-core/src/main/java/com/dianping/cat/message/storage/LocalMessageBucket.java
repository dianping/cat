package com.dianping.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class LocalMessageBucket implements MessageBucket {
	public static final String ID = "local";

	private static final int MAX_BLOCK_SIZE = 1 << 16; // 64K

	@Inject
	private MessageCodec m_codec;

	private File m_baseDir = new File(".");

	private MessageBlockReader m_reader;

	private MessageBlockWriter m_writer;

	private AtomicBoolean m_dirty = new AtomicBoolean();

	private String m_dataFile;

	private long m_lastAccessTime;

	private GZIPOutputStream m_out;

	private ByteArrayOutputStream m_buf;

	private MessageBlock m_block;

	private int m_blockSize;

	@Override
	public void close() throws IOException {
		synchronized (this) {
			if (m_reader != null) {
				m_reader.close();
				m_writer.close();
				m_out.close();
				m_buf.close();
				m_out = null;
				m_buf = null;
				m_reader = null;
				m_writer = null;
			}
		}
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		return findByIndex(index);
	}

	public MessageTree findByIndex(int index) throws IOException {
		try {
			m_lastAccessTime = System.currentTimeMillis();

			byte[] data = m_reader.readMessage(index);
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(data.length);
			MessageTree tree = new DefaultMessageTree();

			buf.writeBytes(data);
			m_codec.decode(buf, tree);
			return tree;
		} catch (EOFException e) {
			Cat.logError(e);
			return null;
		}
	}

	public MessageBlock flushBlock() throws IOException {
		if (m_dirty.get()) {
			synchronized (this) {
				m_out.close();
				byte[] data = m_buf.toByteArray();

				try {
					m_block.setData(data);
					m_blockSize = 0;
					m_buf.reset();
					m_out = new GZIPOutputStream(m_buf);
					m_dirty.set(false);

					return m_block;
				} finally {
					m_block = new MessageBlock(m_dataFile);
				}
			}
		}
		return null;
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	public MessageBlockWriter getWriter() {
		return m_writer;
	}

	@Override
	public void initialize(String dataFile) throws IOException {
		m_dataFile = dataFile;

		File file = new File(m_baseDir, dataFile);

		m_writer = new MessageBlockWriter(file);
		m_reader = new MessageBlockReader(file);
		m_block = new MessageBlock(m_dataFile);
		m_buf = new ByteArrayOutputStream(16384);
		m_out = new GZIPOutputStream(m_buf);
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	public void setMessageCodec(MessageCodec codec) {
		m_codec = codec;
	}

	public MessageBlock storeMessage(final ByteBuf buf, final MessageId id) throws IOException {
		synchronized (this) {
			int size = buf.readableBytes();

			m_dirty.set(true);
			m_lastAccessTime = System.currentTimeMillis();
			m_blockSize += size;
			m_block.addIndex(id.getIndex(), size);
			buf.getBytes(0, m_out, size); // write buffer and compress it

			if (m_blockSize >= MAX_BLOCK_SIZE) {
				return flushBlock();
			} else {
				return null;
			}
		}
	}

}
