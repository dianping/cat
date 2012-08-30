package com.dianping.cat.consumer.dump;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;

public class DumpChannel {
	private MessageCodec m_codec;

	private GZIPOutputStream m_out;
	
	private File m_file;

	private String m_path;

	private long m_maxSize;

	private long m_lastChunkAdjust = 200 * 1024L; // 100K

	private long m_startTime;

	public DumpChannel(MessageCodec codec, File baseDir, String path, long maxSize, long lastChunkAdjust, long startTime)
	      throws IOException {
		m_startTime = startTime;
		m_codec = codec;
		m_file = new File(baseDir, path).getCanonicalFile();
		m_path = path;
		m_maxSize = maxSize;
		m_lastChunkAdjust = lastChunkAdjust;

		m_file.getParentFile().mkdirs();
		m_out = new GZIPOutputStream(new FileOutputStream(m_file), 4096);
	}

	public void close() {
		try {
			m_out.finish();
			m_out.close();
		} catch (IOException e) {
			// ignore it
		}
	}

	public File getFile() {
		return m_file;
	}

	public long getStartTime() {
		return m_startTime;
	}

	public void moveTo(File anotherBase) throws IOException {
		File target = new File(anotherBase, m_path);

		target.getParentFile().mkdirs();

		boolean success = m_file.renameTo(target);

		if (!success) {
			Files.forIO().copy(new FileInputStream(m_file), new FileOutputStream(target));
			m_file.delete();
		}
	}

	public void setLastChunkAdjust(long lastChunkAdjust) {
		m_lastChunkAdjust = lastChunkAdjust;
	}

	public int write(MessageTree tree) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		
		m_codec.encode(tree, buf);

		int length = buf.readInt();
		long count = m_file.length();

		if (m_maxSize > 0 && count + m_lastChunkAdjust + length > m_maxSize) {
			// exceed the max size
			return 0;
		}

		buf.getBytes(buf.readerIndex(), m_out, length);

		// a blank line used to separate two message trees
		m_out.write('\n');
		//m_out.flush();
		
		return length + 1;
	}
}
