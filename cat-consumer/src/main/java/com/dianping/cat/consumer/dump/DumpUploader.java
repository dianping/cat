package com.dianping.cat.consumer.dump;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.helper.Formats;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class DumpUploader implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private FileSystemManager m_fileSystemManager;

	private String m_baseDir;

	private Logger m_logger;

	private Thread m_job;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDir = m_configManager.getHdfsLocalBaseDir("dump");
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_fileSystemManager.getFileSystem("dump", baseDir);
		Path file = new Path(baseDir.toString(), path);
		FSDataOutputStream out = fs.create(file);

		return out;
	}

	public void start() {
		// only start at first time and long running
		if (m_job == null) {
			m_job = Threads.forGroup("Cat").start(new WriteJob());
		}
	}

	private long sleepPeriod = 1000L * 60;

	public void setSleepPeriod(long period) {
		this.sleepPeriod = period;
	}

	class WriteJob implements Task {
		private volatile boolean m_active = true;

		@Override
		public String getName() {
			return "DumpUploader";
		}

		private boolean isActive() {
			synchronized (this) {
				return m_active;
			}
		}

		@Override
		public void run() {
			while (isActive()) {
				try {
					if (Cat.isInitialized()) {
						Calendar cal = Calendar.getInstance();

						if (cal.get(Calendar.MINUTE) >= 10) {
							upload();
						}
					}
				} catch (Exception e) {
					m_logger.warn("Error when dumping message to HDFS. " + e.getMessage());
				}
				try {
					Thread.sleep(sleepPeriod);
				} catch (InterruptedException e) {
					m_active = false;
				}
			}
		}

		@Override
		public void shutdown() {
			synchronized (this) {
				m_active = false;
			}
		}

		private void upload() {
			File baseDir = new File(m_baseDir, "outbox");
			final List<String> paths = new ArrayList<String>();

			Scanners.forDir().scan(baseDir, new FileMatcher() {
				@Override
				public Direction matches(File base, String path) {
					if (new File(base, path).isFile()) {
						paths.add(path);
					}

					return Direction.DOWN;
				}
			});

			int len = paths.size();

			if (len > 0) {
				Cat.setup("DumpUploader");

				MessageProducer cat = Cat.getProducer();
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				Transaction root = cat.newTransaction("System", "Dump-" + ip);

				Collections.sort(paths);

				root.addData("files", paths);
				root.setStatus(Message.SUCCESS);

				for (int i = 0; i < len; i++) {
					String path = paths.get(i);
					Transaction t = cat.newTransaction("System", "UploadDump");
					File file = new File(baseDir, path);

					t.addData("file", path);

					FSDataOutputStream fdos = null;
					try {
						fdos = makeHdfsOutputStream(path);
						FileInputStream fis = new FileInputStream(file);

						long start = System.currentTimeMillis();

						Files.forIO().copy(fis, fdos, AutoClose.INPUT_OUTPUT);

						double sec = (System.currentTimeMillis() - start) / 1000d;
						String size = Formats.forNumber().format(file.length(), "0.#", "B");
						String speed = sec <= 0 ? "N/A" : Formats.forNumber().format(file.length() / sec, "0.0", "B/s");

						t.addData("size", size);
						t.addData("speed", speed);
						t.setStatus(Message.SUCCESS);

						if (!file.delete()) {
							m_logger.warn("Can't delete file: " + file);
						}
					} catch (AlreadyBeingCreatedException e) {
						Cat.logError(e);
						t.setStatus(e);
						m_logger.error(String.format("Already being created (%s)!", path), e);
					} catch (AccessControlException e) {
						cat.logError(e);
						t.setStatus(e);
						m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
					} catch (Exception e) {
						cat.logError(e);
						t.setStatus(e);
						m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", file, path), e);
					} finally {
						try {
							if (fdos != null) {
								fdos.close();
							}
						} catch (IOException e) {
							Cat.logError(e);
						}
						t.complete();
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}

				root.complete();
			}

			// the path has two depth
			for (int i = 0; i < 2; i++) {
				final List<String> directionPaths = new ArrayList<String>();

				Scanners.forDir().scan(baseDir, new FileMatcher() {
					@Override
					public Direction matches(File base, String path) {
						if (new File(base, path).isDirectory()) {
							directionPaths.add(path);
						}

						return Direction.DOWN;
					}
				});
				for (String path : directionPaths) {
					try {
						File file = new File(baseDir, path);

						file.delete();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
