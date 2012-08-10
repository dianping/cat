package com.dianping.cat.storage.dump;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;

public class LocalMessageBucket implements MessageBucket {
	public static final String ID = "local";

	@Inject
	private MessageCodec m_codec;

	@Inject
	private File m_baseDir = new File(".");

	private MessageBlockReader m_reader;

	private MessageBlockWriter m_writer;

	private AtomicBoolean m_dirty = new AtomicBoolean();

	private int m_rawSize;

	private String m_dataFile;

	private long m_lastAccessTime;

	private void checkDirty() throws IOException {
		if (m_dirty.get()) {
			m_writer.flushBlock();
			m_dirty.set(false);
		}
	}

	@Override
	public void close() throws IOException {
		m_reader.close();
		m_writer.close();
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		return findByIndex(index);
	}

	@Override
	public MessageTree findByIndex(int index) throws IOException {
		checkDirty();

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
	}

	public void setBaseDir(File baseDir) {
		m_baseDir = baseDir;
	}

	public void setMessageCodec(MessageCodec codec) {
		m_codec = codec;
	}

	@Override
	public void store(MessageTree tree) throws IOException {
		int index = MessageId.parse(tree.getMessageId()).getIndex();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);
		buf.readInt();// get rid of length

		int size = buf.readableBytes();
		byte[] data = new byte[size];

		buf.readBytes(data);
		m_lastAccessTime = System.currentTimeMillis();
		m_writer.writeMessage(index, data);
		m_dirty.set(true);
		m_rawSize += data.length;
	}

	static class MessageBlockReader {
		private RandomAccessFile m_indexFile;

		private RandomAccessFile m_dataFile;

		public MessageBlockReader(File dataFile) throws IOException {
			File indexFile = new File(dataFile.getAbsolutePath() + ".idx");

			m_indexFile = new RandomAccessFile(indexFile, "r");
			m_dataFile = new RandomAccessFile(dataFile, "r");
		}

		public void close() throws IOException {
			synchronized (m_indexFile) {
				m_indexFile.close();
				m_dataFile.close();
			}
		}

		public byte[] readMessage(int index) throws IOException {
			int blockAddress;
			int blockOffset;
			byte[] buf;

			synchronized (m_indexFile) {
				m_indexFile.seek(index * 6);
				blockAddress = m_indexFile.readInt();
				blockOffset = m_indexFile.readShort() & 0xFFFF;
			}

			synchronized (m_dataFile) {
				m_dataFile.seek(blockAddress);
				buf = new byte[m_dataFile.readInt()];
				m_dataFile.readFully(buf);
			}

			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			DataInputStream in = new DataInputStream(new GZIPInputStream(bais));

			try {
				in.skip(blockOffset);

				int len = in.readInt();
				byte[] data = new byte[len];

				in.readFully(data);
				return data;
			} finally {
				try {
					in.close();
				} catch (Exception e) {
					// ignore it
				}
			}
		}
	}

	static class MessageBlockWriter {
		private static final int MAX_BLOCK_SIZE = 1 << 16; // 64K

		private RandomAccessFile m_indexFile;

		private RandomAccessFile m_dataFile;

		private int m_blockAddress;

		private int m_blockSize;

		private ByteArrayOutputStream m_buf;

		private DataOutputStream m_out;

		public MessageBlockWriter(File dataFile) throws IOException {
			File indexFile = new File(dataFile.getAbsolutePath() + ".idx");

			dataFile.getParentFile().mkdirs();
			m_indexFile = new RandomAccessFile(indexFile, "rw");
			m_dataFile = new RandomAccessFile(dataFile, "rw");
			m_buf = new ByteArrayOutputStream(8192);
			m_out = new DataOutputStream(new GZIPOutputStream(m_buf));
			m_blockAddress = (int) m_dataFile.length();

			m_dataFile.seek(m_blockAddress); // move to end
		}

		public synchronized void close() throws IOException {
			if (m_out != null) {
				try {
					flushBlock();
				} finally {
					m_out.close();
					m_indexFile.close();
					m_dataFile.close();

					m_out = null;
				}
			}
		}

		protected synchronized void flushBlock() throws IOException {
			m_out.close();

			byte[] data = m_buf.toByteArray();

			if (data.length > 0) {
				m_dataFile.writeInt(data.length);
				m_dataFile.write(data);
				m_blockAddress += data.length + 4;
				m_blockSize = 0;
			}

			m_buf.reset();
			m_out = new DataOutputStream(new GZIPOutputStream(m_buf));
		}

		public synchronized void writeMessage(int index, byte[] data) throws IOException {
			if (m_blockSize + data.length > MAX_BLOCK_SIZE) {
				flushBlock();
			}

			m_indexFile.seek(index * 6);
			m_indexFile.writeInt(m_blockAddress);
			m_indexFile.writeShort(m_blockSize & 0xFFFF);
			m_out.writeInt(data.length);
			m_out.write(data);

			m_blockSize += data.length + 4;
		}
	}

	public void archive() throws IOException {
		File outbox = new File(m_baseDir, "outbox");
		File from = new File(m_baseDir, m_dataFile);
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
}
