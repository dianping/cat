package com.dianping.cat.hadoop.hdfs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.message.storage.MessageBucket;

public class HdfsMessageBucket implements MessageBucket {
	public static final String ID = "hdfs";

	@Inject
	private FileSystemManager m_manager;

	@Inject
	private MessageCodec m_codec;

	private MessageBlockReader m_reader;

	private long m_lastAccessTime;

	@Override
	public void close() throws IOException {
		m_reader.close();
	}

	@Override
	public MessageTree findById(String messageId) throws IOException {
		int index = MessageId.parse(messageId).getIndex();

		try {
			byte[] data = m_reader.readMessage(index);
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(data.length);
			MessageTree tree = new DefaultMessageTree();

			buf.writeBytes(data);
			m_codec.decode(buf, tree);
			m_lastAccessTime = System.currentTimeMillis();
			return tree;
		} catch (EOFException e) {
			Cat.logError(e);
			return null;
		}
	}

	@Override
	public long getLastAccessTime() {
		return m_lastAccessTime;
	}

	@Override
	public void initialize(String dataFile) throws IOException {
		m_reader = new MessageBlockReader(m_manager, dataFile);
	}

	public void setMessageCodec(MessageCodec codec) {
		m_codec = codec;
	}

	static class MessageBlockReader {
		private FSDataInputStream m_indexFile;

		private FSDataInputStream m_dataFile;

		public MessageBlockReader(FileSystemManager manager, String dataFile) throws IOException {
			StringBuilder sb = new StringBuilder();
			FileSystem fs = manager.getFileSystem(ServerConfigManager.DUMP_DIR, sb);
			Path basePath = new Path(sb.toString());

			m_indexFile = fs.open(new Path(basePath, dataFile + ".idx"));
			m_dataFile = fs.open(new Path(basePath, dataFile));
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
				m_indexFile.seek(index * 6L);
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
	
}
