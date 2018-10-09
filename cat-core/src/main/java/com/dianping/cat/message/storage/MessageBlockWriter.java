package com.dianping.cat.message.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class MessageBlockWriter {
	private RandomAccessFile m_indexFile;

	private RandomAccessFile m_dataFile;

	private FileChannel m_indexChannel;

	private FileChannel m_dataChannel;

	private int m_blockAddress;

	public MessageBlockWriter(File dataFile) throws IOException {
		File indexFile = new File(dataFile.getAbsolutePath() + ".idx");

		dataFile.getParentFile().mkdirs();
		m_indexFile = new RandomAccessFile(indexFile, "rw");
		m_dataFile = new RandomAccessFile(dataFile, "rw");
		m_indexChannel = m_indexFile.getChannel();
		m_dataChannel = m_dataFile.getChannel();
		m_blockAddress = (int) m_dataFile.length();
		// m_dataFile.seek(m_blockAddress); // move to end
		m_dataChannel.position(m_blockAddress);
	}

	public void close() throws IOException {
		synchronized (m_indexFile) {
			m_indexChannel.close();
			m_dataChannel.close();

			m_indexFile.close();
			m_dataFile.close();
		}
	}

	public synchronized void writeBlock(MessageBlock block) throws IOException {
		int len = block.getBlockSize();
		byte[] data = block.getData();
		int blockSize = 0;

		ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
		buffer.order(ByteOrder.BIG_ENDIAN);

		for (int i = 0; i < len; i++) {
			int seq = block.getIndex(i);
			int size = block.getSize(i);

			// m_indexFile.seek(seq * 6L);
			m_indexChannel.position(seq * 6L);

			// m_indexFile.writeInt(m_blockAddress);
			// m_indexFile.writeShort(blockSize);
			buffer.putInt(m_blockAddress);
			buffer.putShort((short) blockSize);
			buffer.flip();
			m_indexChannel.write(buffer);

			blockSize += size;

			buffer.clear();
		}

		// m_dataFile.writeInt(data.length);
		// m_dataFile.write(data);
		buffer = ByteBuffer.allocate(4 + data.length);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(data.length);
		buffer.put(data);
		buffer.flip();
		m_dataChannel.write(buffer);

		m_blockAddress += data.length + 4;
	}
}
