package com.dianping.cat.consumer.logview;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.dal.Logview;
import com.dianping.cat.hadoop.dal.LogviewDao;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.message.LocalLogviewBucket;
import com.dianping.cat.storage.message.LocalLogviewBucket.Meta;
import com.site.dal.jdbc.DalException;
import com.site.helper.Files;
import com.site.helper.Files.AutoClose;
import com.site.helper.Formats;
import com.site.helper.Joiners;
import com.site.helper.Splitters;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class LogviewUploader implements Task, Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private FileSystemManager m_fileSystemManager;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private LogviewDao m_logviewDao;

	private String m_baseDir;

	private TodoList m_todoList;

	private boolean m_localMode = true;

	private boolean m_active;

	private Logger m_logger;

	public void addBucket(long timestamp, String domain) {
		m_todoList.offer(timestamp + ":" + domain);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDir = m_configManager.getHdfsLocalBaseDir("logview");
		m_localMode = m_configManager.isLocalMode();
		m_todoList = new TodoList(new File(m_baseDir, "TODO"), m_logger);
		m_active = true;
	}

	// TODO try to remove it
	public boolean isLocalMode() {
		return m_localMode;
	}

	@Override
	public void run() {
		try {
			while (m_active) {
				String item = m_todoList.take();
				int pos = item.indexOf(':');
				long timestamp = Long.parseLong(item.substring(0, pos));
				String domain = item.substring(pos + 1);
				LocalLogviewBucket bucket = (LocalLogviewBucket) m_bucketManager.getLogviewBucket(timestamp, domain);

				try {
					upload(timestamp, bucket);
				} catch (Exception e) {
					// add back since it's not done yet
					m_todoList.offer(item);

					throw e;
				}

				Thread.sleep(1000);
			}
		} catch (Exception e) {
			m_logger.error("Error when uploading bucket.", e);
		}
	}

	@Override
	public void shutdown() {
		m_active = false;
	}

	private void upload(long timestamp, LocalLogviewBucket bucket) throws DalException {
		List<String> batchIds = new ArrayList<String>();
		int batchSize = 50;

		uploadData(bucket.getLogicalPath());

		for (String id : bucket.getIds()) {
			batchIds.add(id);

			if (batchIds.size() >= batchSize) {
				uploadIndex(batchIds, timestamp, bucket);
				batchIds.clear();
			}
		}

		if (batchIds.size() > 0) {
			uploadIndex(batchIds, timestamp, bucket);
		}
	}

	private void uploadData(String logicalPath) {
		File file = new File(m_baseDir, logicalPath);

		if (file.exists()) {
			StringBuilder sb = new StringBuilder(32);
			Path path = null;

			try {
				FileSystem fs = m_fileSystemManager.getFileSystem("logview", sb);
				String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				path = new Path(sb.toString(), logicalPath + "-" + ipAddress);

				FileInputStream fis = new FileInputStream(file);
				FSDataOutputStream fdos = fs.create(path);

				long start = System.currentTimeMillis();

				m_logger.info(String.format("Start uploading(%s) to HDFS(%s) ...", file.getCanonicalPath(), path));
				Files.forIO().copy(fis, fdos, AutoClose.INPUT_OUTPUT);

				double sec = (System.currentTimeMillis() - start) / 1000d;
				String size = Formats.forNumber().format(file.length(), "0.#", "B");
				String speed = sec <= 0 ? "N/A" : Formats.forNumber().format(file.length() / sec, "0.0", "B/s");

				m_logger.info(String.format("Finish uploading(%s) to HDFS(%s) with size(%s) at %s.",
				      file.getCanonicalPath(), path, size, speed));
			} catch (AccessControlException e) {
				m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
			} catch (IOException e) {
				m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", file, path), e);
			}
		}
	}

	private void uploadIndex(List<String> ids, long timestamp, LocalLogviewBucket bucket) throws DalException {
		int len = ids.size();
		List<Logview> logviews = new ArrayList<Logview>();
		Date date = new Date(timestamp);
		String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		String path = bucket.getLogicalPath() + "-" + ipAddress;

		for (int i = 0; i < len; i++) {
			String id = ids.get(i);
			Logview l = m_logviewDao.createLocal();
			Meta meta = bucket.getMeta(id);

			l.setDataLength(meta.getLegnth());
			l.setDataOffset(meta.getOffset());
			l.setDataPath(path);
			l.setCreationDate(date);
			l.setMessageId(meta.getMessageId());
			l.setTagThread(meta.getTagThread());
			logviews.add(l);
		}

		m_logviewDao.insert(logviews.toArray(new Logview[0]));
	}

	static class TodoList {
		private BlockingQueue<String> m_queue = new LinkedBlockingQueue<String>();

		private File m_file;

		private Logger m_logger;

		public TodoList(File file, Logger logger) {
			m_file = file;
			m_logger = logger;

			if (m_file.exists()) {
				load();
			}
		}

		private void load() {
			try {
				String content = Files.forIO().readFrom(m_file, "utf-8");
				List<String> items = Splitters.by('\n').noEmptyItem().split(content);

				m_queue.addAll(items);
			} catch (IOException e) {
				m_logger.error("Error when loading TODO list.", e);
			}
		}

		public void offer(String item) {
			if (m_queue.offer(item)) {
				persist();
			}
		}

		private void persist() {
			String content = Joiners.by('\n').join(m_queue);

			try {
				Files.forIO().writeTo(m_file.getCanonicalFile(), content);

				m_logger.info(String.format("TODO file(%s) persisted!", m_file));
			} catch (Exception e) {
				m_logger.error("Error when persisting TODO list.", e);
			}
		}

		public String take() throws InterruptedException {
			try {
				return m_queue.take();
			} finally {
				persist();
			}
		}
	}
}
