package com.dianping.cat.job.spi.mapreduce;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class MessageTreeReaderV2 extends MessageTreeReader {
	private long m_start;

	private long m_pos;

	private long m_end;

	private BlockReader m_in;

	private LongWritable m_key;

	private MessageTreeWritable m_value;

	private int m_fileLen;

	@Override
	public void close() throws IOException {
		if (m_in != null) {
			m_in.close();
		}
	}

	@Override
	public LongWritable getCurrentKey() throws IOException, InterruptedException {
		return m_key;
	}

	@Override
	public MessageTreeWritable getCurrentValue() throws IOException, InterruptedException {
		return m_value;
	}

	public long getFileLength() {
		return m_fileLen;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if (m_start == m_end) {
			return 0;
		} else {
			return Math.min(1.0f, (m_pos - m_start) * 1.0f / (m_end - m_start));
		}
	}

	public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
		System.out.println("Current working directory:" + new File(".").getCanonicalPath());

		FileSplit split = (FileSplit) genericSplit;
		Configuration config = context.getConfiguration();

		// open the file and seek to the start of the split
		Path file = split.getPath();

		System.out.println("Start processing: " + file.getName());

		FileSystem fs = file.getFileSystem(config);
		FSDataInputStream fileIn = fs.open(file);

		m_start = split.getStart();
		m_fileLen = fileIn.available();

		if (m_start != 0) {
			m_start--;
			fileIn.seek(m_start);
		}

		m_in = new BlockReader(file, fileIn, config);
		m_end = m_start + fileIn.available();
		m_pos = m_start;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (m_key == null) {
			m_key = new LongWritable();
		}

		m_key.set(m_pos);

		if (m_value == null) {
			m_value = new MessageTreeWritable();
		}

		int blockSize = m_in.readBlock(m_value);

		m_pos += blockSize;

		if (!m_value.isCompleted()) {
			return false;
		}

		if (blockSize < 0) {
			m_key = null;
			m_value = null;
			return false;
		} else {
			return true;
		}
	}

	protected void showFiles(File file) {
		String[] list = file.list();

		if (list != null) {
			System.out.println(file + ": " + Arrays.asList(list));

			for (String item : list) {
				showFiles(new File(file, item));
			}
		}
	}

	protected void showJars() {
		String classpath = System.getProperty("java.class.path");
		String[] parts = classpath.split(":");

		for (String part : parts) {
			File file = new File(part);

			if (file.isFile()) {
				System.out.println("File: " + file);
			} else if (file.isDirectory()) {
				showFiles(file);
			}
		}
	}

	protected void showProperties() {
		for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}
	}

	static class BlockReader {
		private DataInputStream m_in;

		private PlainTextMessageCodec m_codec;

		private Path m_file;

		private DataInputStream m_block;

		public BlockReader(Path file, InputStream in, Configuration config) {
			int bufferSize = config.getInt("io.file.buffer.size", 8192);

			m_file = file;
			m_in = new DataInputStream(new BufferedInputStream(in, bufferSize));
			m_codec = new PlainTextMessageCodec();
			m_codec.setBufferWriter(new EscapingBufferWriter());
		}

		public void close() throws IOException {
			m_in.close();

			if (m_block != null) {
				m_block.close();
			}
		}

		public int readBlock(MessageTreeWritable tree) throws IOException {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
			int total = 0;

			do {
				if (m_block == null) {
					try {
						int size = m_in.readInt();
						byte[] blockData = new byte[size];

						m_in.readFully(blockData);
						m_block = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(blockData)));
						total += size + 4;
					} catch (EOFException e) {
						return -1;
					}
				}

				try {
					int len = m_block.readInt();
					byte[] data = new byte[len];

					m_block.readFully(data);
					buf.writeBytes(data);
				} catch (EOFException e) {
					m_block = null; // to next block
				}
			} while (m_block == null);

			try {
				m_codec.decode(buf, tree.get());
				tree.complete();
			} catch (Throwable e) {
				System.out.println("Error when parsing file: " + m_file);
				e.printStackTrace(System.out);
				System.out.println("The message tree is: " + tree.get());
			}

			return total;
		}
	}
}
