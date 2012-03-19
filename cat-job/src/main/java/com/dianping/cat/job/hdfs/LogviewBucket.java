package com.dianping.cat.job.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import com.dianping.cat.job.sql.dal.Logview;
import com.dianping.cat.job.sql.dal.LogviewDao;
import com.dianping.cat.job.sql.dal.LogviewEntity;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.hdfs.HdfsDataStore;
import com.site.dal.jdbc.DalException;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Mar 9, 2012
 */
public class LogviewBucket extends ContainerHolder implements Bucket<byte[]> {
	@Inject
	private LogviewDao logviewDao;

	private HdfsDataStore dataStore;

	private String logicalPath;

	private String hdfsPath;

	@Override
	public void close() throws IOException {
		this.dataStore.close();
	}

	@Override
	public void deleteAndCreate() throws IOException {
		this.initialize(null, null, this.logicalPath);
	}

	@Override
	public byte[] findById(String id) throws IOException {
		Logview logview;
		try {
			logview = this.logviewDao.findByPK(id, LogviewEntity.READSET_FULL);
		} catch (DalException e) {
			throw new IOException(e);
		}
		if (logview == null) {
			return null;
		}
		return dataStore.get(logview.getDataOffset(), logview.getDataLength());
	}

	protected byte[] findByIdAndTag(String id, String tagName, boolean direction) throws IOException {
		String tagThread = null;
		String tagSession = null;
		String tagRequest = null;
		if (tagName.startsWith("r:")) {
			tagRequest = tagName;
		}
		if (tagName.startsWith("s:")) {
			tagSession = tagName;
		}
		if (tagName.startsWith("t:")) {
			tagThread = tagName;
		}
		Logview logview;
		try {
			logview = this.logviewDao.findNextByMessageIdTags(id, direction, tagThread, tagSession, tagRequest, LogviewEntity.READSET_FULL);
		} catch (DalException e) {
			throw new IOException(e);
		}
		if (logview == null) {
			return null;
		}
		return dataStore.get(logview.getDataOffset(), logview.getDataLength());
	}

	@Override
	public byte[] findNextById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, true);
	}

	@Override
	public byte[] findPreviousById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, false);
	}

	@Override
	public void flush() throws IOException {
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
		Logview logview;
		try {
			logview = this.logviewDao.findByPK(id, LogviewEntity.READSET_FULL);
		} catch (DalException e) {
			throw new IOException(e);
		}
		if (logview != null) {
			return false;
		}
		Logview logView = new Logview();
		logView.setDataOffset(this.dataStore.length());
		logView.setDataLength(data.length);
		logView.setDataPath(this.hdfsPath);
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
		try {
			this.logviewDao.insert(logView);
		} catch (DalException e) {
			throw new IOException(e);
		}
		return true;
	}

}
