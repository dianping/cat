package com.dianping.cat.job.mapreduce;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class MessageTreeReader extends RecordReader<LongWritable, MessageTreeWritable> {
	private CompressionCodecFactory m_compressionCodecs;

	private long m_start;

	private long m_pos;

	private long m_end;

	private BlockReader m_in;

	private LongWritable m_key;

	private MessageTreeWritable m_value;

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

	@Override
	public float getProgress() throws IOException, InterruptedException {
		if (m_start == m_end) {
			return 0;
		} else {
			return Math.min(1.0f, (m_pos - m_start) / (float) (m_end - m_start));
		}
	}

	public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
		FileSplit split = (FileSplit) genericSplit;
		Configuration config = context.getConfiguration();

		m_start = split.getStart();
		m_end = m_start + split.getLength();
		m_compressionCodecs = new CompressionCodecFactory(config);

		// open the file and seek to the start of the split
		Path file = split.getPath();
		CompressionCodec codec = m_compressionCodecs.getCodec(file);
		FileSystem fs = file.getFileSystem(config);
		FSDataInputStream fileIn = fs.open(split.getPath());
		boolean skipFirstLine = false;

		if (codec != null) {
			m_in = new BlockReader(codec.createInputStream(fileIn), config);
			m_end = Long.MAX_VALUE;
		} else {
			if (m_start != 0) {
				skipFirstLine = true;
				--m_start;
				fileIn.seek(m_start);
			}

			m_in = new BlockReader(fileIn, config);
		}

		if (skipFirstLine) { // skip first line and re-establish "start".
			m_start += m_in.readBlock(new MessageTreeWritable());
		}

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

		int blockSize = 0;

		if (m_pos < m_end) {
			blockSize = m_in.readBlock(m_value);
			m_pos += blockSize;
		}

		if (blockSize == 0) {
			m_key = null;
			m_value = null;
			return false;
		} else {
			return true;
		}
	}

	static class BlockReader {
		private BufferedInputStream m_in;

		private PlainTextMessageCodec m_codec;

		public BlockReader(InputStream in, Configuration config) {
			int bufferSize = config.getInt("io.file.buffer.size", 8192);

			m_in = new BufferedInputStream(in, bufferSize);
			m_codec = new PlainTextMessageCodec();
			m_codec.setBufferWriter(new EscapingBufferWriter());
		}

		public void close() throws IOException {
			m_in.close();
		}

		public int readBlock(MessageTreeWritable tree) throws IOException {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
			byte[] data = new byte[2048];
			byte prev = 0;
			int count = 0;

			m_in.mark(Integer.MAX_VALUE);

			int size = m_in.read(data);
			int pos = 0;

			while (size >= 0) {
				if (pos >= size) {
					buf.writeBytes(data, 0, size);
					count += size;
					m_in.mark(Integer.MAX_VALUE);
					size = m_in.read(data);
					pos = 0;

					if (size < 0) {
						break;
					}
				}

				byte b = data[pos++];

				if (b == '\n' && prev == '\n') {
					buf.writeBytes(data, 0, pos - 1);
					count += pos;
					m_in.reset();
					m_in.skip(pos);
					break;
				}

				prev = b;
			}

			m_codec.decode(buf, tree.get());

			return count;
		}
	}
}
