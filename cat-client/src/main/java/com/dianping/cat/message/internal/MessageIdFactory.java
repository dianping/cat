package com.dianping.cat.message.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.configuration.NetworkInterfaceManager;

public class MessageIdFactory {
	private long m_lastTimestamp = getTimestamp();

	private volatile int m_index;

	private String m_domain;

	private String m_ipAddress;

	private MappedByteBuffer m_byteBuffer;

	private RandomAccessFile m_markFile;

	private static final long HOUR = 3600 * 1000L;

	public void close() {
		try {
			m_markFile.close();
		} catch (Exception e) {
			// ignore it
		}
	}

	public String getNextId() {
		long timestamp = getTimestamp();
		int index;

		synchronized (this) {
			if (timestamp != m_lastTimestamp) {
				m_index = 0;
				m_lastTimestamp = timestamp;
			}

			index = m_index++;
			saveMark();
		}

		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(m_domain);
		sb.append('-');
		sb.append(m_ipAddress);
		sb.append('-');
		sb.append(timestamp);
		sb.append('-');
		sb.append(index);

		return sb.toString();
	}

	protected long getTimestamp() {
		long timestamp = MilliSecondTimer.currentTimeMillis();

		return timestamp / HOUR; // version 2
	}

	public void initialize(String domain) throws IOException {
		m_domain = domain;

		if (m_ipAddress == null) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			List<String> items = Splitters.by(".").noEmptyItem().split(ip);
			byte[] bytes = new byte[4];

			for (int i = 0; i < 4; i++) {
				bytes[i] = (byte) Integer.parseInt(items.get(i));
			}

			StringBuilder sb = new StringBuilder(bytes.length / 2);

			for (byte b : bytes) {
				sb.append(Integer.toHexString((b >> 4) & 0x0F));
				sb.append(Integer.toHexString(b & 0x0F));
			}

			m_ipAddress = sb.toString();
		}

		String tmpDir = System.getProperty("java.io.tmpdir");
		File mark = new File(tmpDir, "cat-" + domain + ".mark");

		System.out.println("Message id mark file: " + mark.getCanonicalPath());

		m_markFile = new RandomAccessFile(mark, "rw");
		m_byteBuffer = m_markFile.getChannel().map(MapMode.READ_WRITE, 0, 20);

		if (m_byteBuffer.limit() > 0) {
			int index = m_byteBuffer.getInt();
			long timestamp = m_byteBuffer.getLong();

			if (timestamp == m_lastTimestamp) { // for same hour
				m_index = index;
			} else {
				m_index = 0;
			}
		}

		saveMark();
	}

	private void saveMark() {
		try {
			m_byteBuffer.rewind();
			m_byteBuffer.putInt(m_index);
			m_byteBuffer.putLong(m_lastTimestamp);
		} catch (Exception e) {
			// ignore it
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	protected void resetIndex() {
		m_index = 0;
	}
}
