package com.dianping.cat.hadoop.hdfs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.xerial.snappy.SnappyInputStream;

import com.dianping.cat.Cat;

public class MessageBlockReader {
	private FSDataInputStream m_indexFile;

	private FSDataInputStream m_dataFile;

	public MessageBlockReader(FileSystem fs, Path basePath, String dataFile) throws IOException {
		m_indexFile = fs.open(new Path(basePath, dataFile + ".idx"));
		m_dataFile = fs.open(new Path(basePath, dataFile));
	}

	public MessageBlockReader(FileSystem fs, String dataFile) throws IOException {
		m_indexFile = fs.open(new Path(dataFile + ".idx"));
		m_dataFile = fs.open(new Path(dataFile));
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
