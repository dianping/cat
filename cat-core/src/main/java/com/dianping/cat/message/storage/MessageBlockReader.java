package com.dianping.cat.message.storage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

import org.xerial.snappy.SnappyInputStream;

import com.dianping.cat.Cat;

public class MessageBlockReader {
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

	private DataInputStream createDataInputStream(byte[] buf) {
		DataInputStream in = null;

		try {
			in = new DataInputStream(new SnappyInputStream(new ByteArrayInputStream(buf)));
		} catch (IOException e) {
			try {
				in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(buf)));
			} catch (IOException ioe) {
				Cat.logError(ioe);
			}
		}
		return in;
	}

	public byte[] readMessage(int index) throws IOException {
		int blockAddress = 0;
		int blockOffset = 0;

		m_indexFile.seek(index * 6L);
		blockAddress = m_indexFile.readInt();
		blockOffset = m_indexFile.readShort() & 0xFFFF;

		m_dataFile.seek(blockAddress);
		byte[] buf = new byte[m_dataFile.readInt()];
		m_dataFile.readFully(buf);

		DataInputStream in = createDataInputStream(buf);

		if (in != null) {
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
		} else {
			return null;
		}
	}
}