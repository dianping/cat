package com.dianping.cat.job.storage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

import com.dianping.cat.job.sql.dal.Logview;
import com.dianping.cat.job.sql.dal.LogviewDao;
import com.dianping.cat.job.sql.dal.LogviewEntity;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.hdfs.HdfsDataStore;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class RemoteMessageBucket implements Bucket<MessageTree> {
	@Inject
	private MessageCodec codec;
	
	@Inject
	private LogviewDao logviewDao;

	private HdfsDataStore dataStore;

	private String logicalPath;

	private String hdfsPath;
	
	public void setCodec(MessageCodec codec) {
		this.codec = codec;
	}
	
	@Override
	public void close() throws IOException {
		this.dataStore.close();
	}

	@Override
	public void deleteAndCreate() throws IOException {
		this.initialize(null, null, this.logicalPath);
	}

	@Override
	public MessageTree findById(String id) throws IOException {
		Logview logview;
		try {
			logview = this.logviewDao.findByPK(id, LogviewEntity.READSET_FULL);
		} catch (DalException e) {
			throw new IOException(e);
		}
		if (logview == null) {
			return null;
		}
		 byte[] bytes = dataStore.get(logview.getDataOffset(), logview.getDataLength());
		 this.codec.
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
	public MessageTree findNextById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, true);
	}

	@Override
	public MessageTree findPreviousById(String id, String tagName) throws IOException {
		return findByIdAndTag(id, tagName, false);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public Collection<String> getIdsByPrefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

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
	public boolean storeById(String id, MessageTree data) throws IOException {
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
