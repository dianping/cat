package com.dianping.cat.consumer.dump;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.storage.LocalMessageBucket;

public class LogviewUploader implements Task {

	private File m_baseDir;

	private LocalMessageBucketManager m_bucketManager;

	private ConcurrentHashMap<String, LocalMessageBucket> m_buckets;

	private HdfsUploader m_logviewUploader;

	private ServerConfigManager m_configManager;

	private static final long ONE_HOUR = 60 * 60 * 1000L;

	public LogviewUploader(LocalMessageBucketManager bucketManager,
	      ConcurrentHashMap<String, LocalMessageBucket> buckets, HdfsUploader logviewUploader,
	      ServerConfigManager configManager) {
		m_baseDir = new File(configManager.getHdfsLocalBaseDir(ServerConfigManager.DUMP_DIR));
		m_bucketManager = bucketManager;
		m_buckets = buckets;
		m_logviewUploader = logviewUploader;
		m_configManager = configManager;
	}

	private void closeBuckets(final List<String> paths) {
		for (String path : paths) {
			LocalMessageBucket bucket = m_buckets.get(path);

			if (bucket != null) {
				try {
					bucket.close();
				} catch (Exception e) {
					Cat.logError(e);
				} finally {
					m_buckets.remove(path);
					m_bucketManager.releaseBucket(bucket);
				}
			}
		}
	}

	private void deleteFile(String path) {
		File file = new File(m_baseDir, path);
		File parent = file.getParentFile();

		file.delete();
		parent.delete(); // delete it if empty
		parent.getParentFile().delete(); // delete it if empty
	}

	private void deleteOldMessages() {
		final List<String> paths = new ArrayList<String>();
		final Set<String> validPaths = findValidPath(m_configManager.getLogViewStroageTime());

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (path.indexOf(".idx") == -1 && shouldDelete(path)) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}

			private boolean shouldDelete(String path) {
				for (String str : validPaths) {
					if (path.contains(str)) {
						return false;
					}
				}
				return true;
			}
		});

		if (paths.size() > 0) {
			processLogviewFiles(paths, false);
		}
	}

	private List<String> findCloseBuckets() {
		final List<String> paths = new ArrayList<String>();

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (path.indexOf(".idx") == -1 && shouldUpload(path)) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});
		return paths;
	}

	private Set<String> findValidPath(int storageDays) {
		Set<String> strs = new HashSet<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long currentTimeMillis = System.currentTimeMillis();

		for (int i = 0; i < storageDays; i++) {
			Date date = new Date(currentTimeMillis - i * 24 * 60 * 60 * 1000L);

			strs.add(sdf.format(date));
		}
		return strs;
	}

	@Override
	public String getName() {
		return "LocalMessageBucketManager-OldMessageMover";
	}

	private void processLogviewFiles(final List<String> paths, boolean upload) {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		Transaction t = Cat.newTransaction("System", "Delete" + "-" + ip);

		t.setStatus(Message.SUCCESS);
		t.addData("upload", String.valueOf(upload));

		for (String path : paths) {
			File file = new File(m_baseDir, path);
			String loginfo = "path:" + m_baseDir + "/" + path + ",file size: " + file.length();

			try {
				if (upload) {
					uploadFile(path);
					uploadFile(path + ".idx");
					Cat.getProducer().logEvent("Upload", "UploadAndDelete", Message.SUCCESS, loginfo);
				} else {
					deleteFile(path);
					deleteFile(path + ".idx");
					Cat.getProducer().logEvent("Upload", "Delete", Message.SUCCESS, loginfo);
				}
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			}
		}
		t.complete();
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			try {
				if (m_configManager.isHdfsOn()) {
					long current = System.currentTimeMillis() / 1000 / 60;
					int min = (int) (current % (60));

					// make system 0-10 min is not busy
					if (min > 10) {
						List<String> paths = findCloseBuckets();

						closeBuckets(paths);
						processLogviewFiles(paths, true);
					}
				} else {
					// for clean java memory
					List<String> paths = findCloseBuckets();

					closeBuckets(paths);
					deleteOldMessages();
				}
			} catch (Throwable e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(2 * 60 * 1000L);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private boolean shouldUpload(String path) {
		if (path.indexOf("draft") > -1 || path.indexOf("outbox") > -1) {
			return false;
		}
		long current = System.currentTimeMillis();
		long currentHour = current - current % ONE_HOUR;
		long lastHour = currentHour - ONE_HOUR;
		long nextHour = currentHour + ONE_HOUR;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/HH");
		String currentHourStr = sdf.format(new Date(currentHour));
		String lastHourStr = sdf.format(new Date(lastHour));
		String nextHourStr = sdf.format(new Date(nextHour));

		int indexOf = path.indexOf(currentHourStr);
		int indexOfLast = path.indexOf(lastHourStr);
		int indexOfNext = path.indexOf(nextHourStr);

		if (indexOf > -1 || indexOfLast > -1 || indexOfNext > -1) {
			return false;
		}
		return true;
	}

	@Override
	public void shutdown() {
	}

	private void uploadFile(String path) {
		File file = new File(m_baseDir, path);
		boolean success = m_logviewUploader.uploadLogviewFile(path, file);

		if (success) {
			deleteFile(path);
		}
	}

}