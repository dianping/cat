package com.dianping.cat.storage.dump;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

public class LocalMessageBucket implements MessageBucket {
	public static final String ID = "local";

	private static final int MAX_BLOCK_SIZE = 1 << 16; // 64K

	@Inject
	private MessageCodec m_codec;

	@Inject
	private ChannelBufferManager m_bufferManager;

	@Inject
	private File m_baseDir = new File(".");

	private MessageBlockReader m_reader;

	private MessageBlockWriter m_writer;

	private AtomicBoolean m_dirty = new AtomicBoolean();

	private int m_rawSize;

	private String m_dataFile;

	private long m_lastAccessTime;

	private GZIPOutputStream m_out;

	private ByteArrayOutputStream m_buf;

	private MessageBlock m_block;

	private int m_blockSize;

	public void archive() throws IOException {
		File from = new File(m_baseDir, m_dataFile);

		Cat.getProducer().logEvent("Dump", "Outbox.Normal", Message.SUCCESS, from.getPath());

		File outbox = new File(m_baseDir, "outbox");
		File to = new File(outbox, m_dataFile);
		File fromIndex = new File(m_baseDir, m_dataFile + ".idx");
		File toIndex = new File(outbox, m_dataFile + ".idx");

		to.getParentFile().mkdirs();
		Files.forDir().copyFile(from, to);
		Files.forDir().copyFile(fromIndex, toIndex);
		Files.forDir().delete(from);
		Files.forDir().delete(fromIndex);

		File parentFile = from.getParentFile();

		parentFile.delete(); // delete it if empty
		parentFile.getParentFile().delete(); // delete it if empty
	}

	@Override
	public void close() throws IOException {
		if (m_reader != null) {
			m_reader.close();
			m_writer.close();
			m_reader = null;
			m_writer = null;
		}
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		return findByIndex(index);
	}

	@Override
	public MessageTree findByIndex(int index) throws IOException {
		try {
			m_lastAccessTime = System.currentTimeMillis();

			byte[] data = m_reader.readMessage(index);
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(data.length);
			MessageTree tree = new DefaultMessageTree();

			buf.writeBytes(data);
			m_codec.decode(buf, tree);
			return tree;
		} catch (EOFException e) {
			return null;
		}
	}

	protected MessageBlock flushBlock() throws IOException {
		boolean b = m_dirty.get();

		if (b) {
			synchronized (m_out) {
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

	public MessageBlockWriter getWriter() {
		return m_writer;
	}

	public double getCompressionRate() {
		return m_rawSize * 1.0 / m_dataFile.length();
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
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

	@Override
	public MessageBlock store(final MessageTree tree, final MessageId id) throws IOException {
		final ChannelBuffer buf = m_bufferManager.allocate();
	
		m_codec.encode(tree, buf);

		return storeMessage(buf, id);
	}

	synchronized MessageBlock storeMessage(final ChannelBuffer buf, final MessageId id) throws IOException {
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
