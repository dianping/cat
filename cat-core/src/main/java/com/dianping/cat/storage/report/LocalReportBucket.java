package com.dianping.cat.storage.report;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.storage.Bucket;
import com.site.helper.Splitters;
import com.site.helper.Splitters.StringSplitter;
import com.site.lookup.annotation.Inject;

public class LocalReportBucket implements Bucket<String>, LogEnabled {
	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private String m_baseDir = "target/bucket";

	// key => offset of record
	private Map<String, Long> m_idToOffsets = new HashMap<String, Long>();

	// tag => list of ids
	private Map<String, List<String>> m_tagToIds = new HashMap<String, List<String>>();

	private ReentrantLock m_readLock;

	private RandomAccessFile m_readDataFile;

	private ReentrantLock m_writeLock;

	private long m_writeDataFileLength;

	private OutputStream m_writeDataFile;

	private OutputStream m_writeIndexFile;

	private Logger m_logger;

	private String m_logicalPath;

	@Override
	public void close() throws IOException {
		m_writeLock.lock();

		try {
			m_idToOffsets.clear();
			m_tagToIds.clear();
			m_writeDataFile.close();
			m_writeIndexFile.close();
		} catch (Exception e) {
			// ignore it
		} finally {
			m_writeLock.unlock();
		}
	}

	@Override
	public void deleteAndCreate() throws IOException {
		File dataFile = new File(m_baseDir, m_logicalPath);
		File indexFile = new File(m_baseDir, m_logicalPath + ".idx");

		dataFile.delete();
		indexFile.delete();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<String> findAllById(String id) throws IOException {
		throw new UnsupportedOperationException("Not supported by local logview bucket!");
	}

	@Override
	public String findById(String id) throws IOException {
		Long offset = m_idToOffsets.get(id);

		if (offset != null) {
			m_readLock.lock();

			try {
				m_readDataFile.seek(offset);

				int num = Integer.parseInt(m_readDataFile.readLine());
				byte[] bytes = new byte[num];

				m_readDataFile.readFully(bytes);

				return new String(bytes, "utf-8");
			} catch (Exception e) {
				m_logger.error(String.format("Error when reading file(%s)!", m_readDataFile), e);
			} finally {
				m_readLock.unlock();
			}
		}

		return null;
	}

	@Override
	public String findNextById(String id, String tag) throws IOException {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			index++;

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public String findPreviousById(String id, String tag) throws IOException {
		List<String> ids = m_tagToIds.get(tag);

		if (ids != null) {
			int index = ids.indexOf(id);

			index--;

			if (index >= 0 && index < ids.size()) {
				String nextId = ids.get(index);

				return findById(nextId);
			}
		}

		return null;
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public Collection<String> getIdsByPrefix(String tag) {
		throw new UnsupportedOperationException("Not supported by local logview bucket!");
	}

	@Override
	public void initialize(Class<?> type, String name, Date timestamp) throws IOException {
		m_writeLock = new ReentrantLock();
		m_readLock = new ReentrantLock();

		String logicalPath = m_pathBuilder.getReportPath(name, timestamp);

		File dataFile = new File(m_baseDir, logicalPath);
		File indexFile = new File(m_baseDir, logicalPath + ".idx");

		dataFile.getParentFile().mkdirs();

		m_logicalPath = logicalPath;
		m_writeDataFile = new BufferedOutputStream(new FileOutputStream(dataFile), 8192);
		m_writeIndexFile = new BufferedOutputStream(new FileOutputStream(indexFile), 8192);
		m_readDataFile = new RandomAccessFile(dataFile, "r");

		if (indexFile.exists()) {
			loadIndexes(indexFile);
		}
	}

	protected void loadIndexes(File indexFile) throws IOException {
		m_writeLock.lock();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(indexFile));
			StringSplitter splitter = Splitters.by('\t');

			while (true) {
				String line = reader.readLine();

				if (line == null) { // EOF
					break;
				}

				List<String> parts = splitter.split(line);

				if (parts.size() >= 2) {
					String id = parts.remove(0);
					String offset = parts.remove(0);

					try {
						m_idToOffsets.put(id, Long.parseLong(offset));
					} catch (NumberFormatException e) {
						// ignore it
					}
				}
			}
		} finally {
			m_writeLock.unlock();
		}
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	@Override
	public boolean storeById(String id, String report) throws IOException {
		m_writeLock.lock();

		if (m_idToOffsets.containsKey(id)) {
			return false;
		}

		byte[] content = report.getBytes("utf-8");
		int length = content.length;
		byte[] num = String.valueOf(length).getBytes("utf-8");

		try {
			m_writeDataFile.write(num);
			m_writeDataFile.write('\n');
			m_writeDataFile.write(content);
			m_writeDataFile.write('\n');
			m_writeDataFile.flush();

			long offset = m_writeDataFileLength;
			String line = id + '\t' + offset + '\n';
			byte[] data = line.getBytes("utf-8");

			m_writeDataFileLength += num.length + 1 + length + 1;
			m_writeIndexFile.write(data);
			m_writeDataFile.flush();
			m_idToOffsets.put(id, offset);
			return true;
		} finally {
			m_writeLock.unlock();
		}
	}
}
