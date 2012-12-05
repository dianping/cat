package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class MessageBlockWriter {
	private RandomAccessFile m_indexFile;

	private RandomAccessFile m_dataFile;

	private int m_blockAddress;

	public MessageBlockWriter(File dataFile) throws IOException {
		File indexFile = new File(dataFile.getAbsolutePath() + ".idx");

		dataFile.getParentFile().mkdirs();
		m_indexFile = new RandomAccessFile(indexFile, "rw");
		m_dataFile = new RandomAccessFile(dataFile, "rw");
		m_blockAddress = (int) m_dataFile.length();
		m_dataFile.seek(m_blockAddress); // move to end
	}

	public synchronized void close() throws IOException {
		m_indexFile.close();
		m_dataFile.close();
	}

	public synchronized void writeBlock(MessageBlock block) throws IOException {
		int len = block.getBlockSize();
		byte[] data = block.getData();
		int blockSize = 0;

		for (int i = 0; i < len; i++) {
			int seq = block.getIndex(i);
			int size = block.getSize(i);

			m_indexFile.seek(seq * 6);
			m_indexFile.writeInt(m_blockAddress);
			m_indexFile.writeShort(blockSize);
			blockSize += size;
		}

		m_dataFile.writeInt(data.length);
		m_dataFile.write(data);
		m_blockAddress += data.length + 4;
	}
}
