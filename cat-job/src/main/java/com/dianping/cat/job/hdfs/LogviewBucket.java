package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.hdfs.HdfsDataStore;
import com.dianping.cat.storage.mysql.LogView;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class LogviewBucket implements Bucket<byte[]> {

	private LogViewIndexStore indexStore;

	private HdfsDataStore dataStore;

	private String logicalPath;

	private String hdfsPath;

	@Override
	public void close() throws IOException {
		this.indexStore.close();
	}

	@Override
	public void deleteAndCreate() throws IOException {
		this.initialize(null, null, this.logicalPath);
	}

	@Override
	public List<byte[]> findAllByIds(List<String> ids) throws IOException {
		List<byte[]> values = new ArrayList<byte[]>(ids.size());
		for (String id : ids) {
			byte[] value = this.findById(id);
			values.add(value);
		}
		return values;
	}

	@Override
	public List<String> findAllIdsByTag(String tag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] findById(String id) throws IOException {
		LogView logview = this.indexStore.getLogViewByMessageId(id);
		if (logview == null) {
			return null;
		}
		return dataStore.get(logview.getOffset(), logview.getLength());
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public byte[] findPreviousById(String id, String tagName) throws IOException {
		LogView logview = this.indexStore.getLogViewByMessageId(id);
		if (logview == null) {
			return null;
		}
		return dataStore.get(logview.getOffset(), logview.getLength());
	}

	@Override
	public byte[] findNextById(String id, String tagName) throws IOException {
		LogView logview = this.indexStore.getLogViewByMessageId(id);
		if (logview == null) {
			return null;
		}
		return dataStore.get(logview.getOffset(), logview.getLength());
	}

	/**
	 * @param baseDir
	 *            e.g /data/appdata/cat/
	 * @param logicalPath
	 *            e.g /a/b/c
	 */
	@Override
	public void initialize(Class<?> type, File baseDir, String logicalPath) throws IOException {
		this.logicalPath = logicalPath;

		File file = new File(baseDir, logicalPath);
		File parent = file.getParentFile();
		parent.mkdirs();

		String hdfsDir = parent.getAbsolutePath();
		String filename = new File(logicalPath).getName() + "-" + InetAddress.getLocalHost().getHostAddress();
		this.hdfsPath = new File(hdfsDir, filename).getAbsolutePath();
	}

	@Override
	public boolean storeById(String id, byte[] data, String... tags) throws IOException {
		LogView logView = new LogView();
		logView.setOffset(this.dataStore.length());
		logView.setLength(data.length);
		logView.setPath(this.hdfsPath);
		if (tags != null) {
			for (String tag : tags) {
				if (tag.startsWith("r:")) {
					logView.setTagRequest(tag);
				}
				if (tag.startsWith("s:")) {
					logView.setTagSession(tag);
				}
				if (tag.startsWith("t:")) {
					logView.setTagThread(tag);
				}
			}
		}
		this.dataStore.append(data);
		return this.indexStore.insert(logView);
	}

}
