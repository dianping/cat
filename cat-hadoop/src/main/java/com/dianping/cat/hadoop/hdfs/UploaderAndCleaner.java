package com.dianping.cat.hadoop.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.ipc.RemoteException;
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
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class UploaderAndCleaner implements Initializable, Task, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private FileSystemManager m_fileSystemManager;

	private String m_dumpBaseDir;

	private String m_reportBaseDir;

	private Logger m_logger;

	private long m_sleepPeriod = 1000L * 60;

	private volatile boolean m_active = true;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "DumpUploader";
	}

	@Override
	public void initialize() throws InitializationException {
		m_dumpBaseDir = m_configManager.getHdfsLocalBaseDir("dump");
		m_reportBaseDir = m_configManager.getHdfsLocalBaseDir("logview");
	}

	private boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_fileSystemManager.getFileSystem("dump", baseDir);
		Path file = new Path(baseDir.toString(), path);
		FSDataOutputStream out;

		try {
			out = fs.create(file);
		} catch (RemoteException re) {
			fs.delete(file, false);

			out = fs.create(file);
		} catch (AlreadyBeingCreatedException e) {
			fs.delete(file, false);

			out = fs.create(file);
		}
		return out;
	}

	@Override
	public void run() {
		while (isActive()) {
			try {
				if (Cat.isInitialized()) {
					Calendar cal = Calendar.getInstance();

					if (cal.get(Calendar.MINUTE) >= 10) {
						uploadLogviewFile();
						deleteOldReports();
					}
				}
			} catch (Exception e) {
				m_logger.warn("Error when dumping message to HDFS. " + e.getMessage());
			}
			try {
				Thread.sleep(m_sleepPeriod);
			} catch (InterruptedException e) {
				m_active = false;
			}
		}
	}

	public void setSleepPeriod(long period) {
		m_sleepPeriod = period;
	}

	@Override
	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}

	public void deleteOldReports() {
		Transaction t = Cat.newTransaction("System", "DeleteReport");
		try {
			File reportDir = new File(m_reportBaseDir);
			final List<String> toRemovePaths = new ArrayList<String>();
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			final String today = sdf.format(date);
			final String yesterday = sdf.format(new Date(date.getTime() - 24 * 60 * 60 * 1000L));

			Scanners.forDir().scan(reportDir, new FileMatcher() {
				@Override
				public Direction matches(File base, String path) {
					File file = new File(base, path);
					if (file.isFile() && shouldDeleteReport(path)) {
						toRemovePaths.add(path);
					}
					return Direction.DOWN;
				}

				private boolean shouldDeleteReport(String path) {
					if (path.indexOf(today) > -1 || path.indexOf(yesterday) > -1) {
						return false;
					} else {
						return true;
					}
				}
			});
			for (String path : toRemovePaths) {
				File file = new File(m_reportBaseDir, path);

				file.delete();
				Cat.logEvent("System", "DeleteReport", Event.SUCCESS, file.getAbsolutePath());
			}
			removeEmptyDir(reportDir);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private void removeEmptyDir(File baseFile) {
		// the path has two depth
		for (int i = 0; i < 2; i++) {
			final List<String> directionPaths = new ArrayList<String>();

			Scanners.forDir().scan(baseFile, new FileMatcher() {
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
					File file = new File(baseFile, path);

					file.delete();
				} catch (Exception e) {
				}
			}
		}
	}

	private void uploadLogviewFile() {
		File baseDir = new File(m_dumpBaseDir, "outbox");
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
				FileInputStream fis = null;
				try {
					fdos = makeHdfsOutputStream(path);
					fis = new FileInputStream(file);

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
					Thread.sleep(500);
				} catch (InterruptedException e) {
					break;
				}
			}

			root.complete();
		}
		removeEmptyDir(baseDir);
	}
}
