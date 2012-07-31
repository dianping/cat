/**
 * 
 */
package com.dianping.cat.consumer.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.site.helper.Files;
import com.site.helper.Files.AutoClose;
import com.site.helper.Formats;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Jun 21, 2012
 */
public class RemoteIdUploader implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private FileSystemManager m_fileSystemManager;

	private Logger m_logger;

	private boolean m_localMode = true;

	private Thread m_job;

	private String m_baseDir;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();

		if (!m_localMode) {
			m_baseDir = m_configManager.getHdfsLocalBaseDir("remote");

		}
	}

	public void start() {
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
			return "RemoteIdUploader";
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
						upload();
					}
				} catch (Exception e) {
					m_logger.error("Error when dumping remoteIds to HDFS. " + e.getMessage());
				}

				try {
					Thread.sleep(sleepPeriod);
				} catch (InterruptedException e) {
					//ignore it
				}
			}
		}

		@Override
		public void shutdown() {
			synchronized (this) {
				m_active = false;
			}
		}

		private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
			StringBuilder baseDir = new StringBuilder(32);
			FileSystem fs = m_fileSystemManager.getFileSystem("remote", baseDir);
			Path file = new Path(baseDir.toString(), path);
			FSDataOutputStream out = fs.create(file);
			return out;
		}

		private void upload() {
			File outbox = new File(m_baseDir, "outbox");
			String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			Date lastHour = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
			String path = m_builder.getMessageRemoteIdPath(ipAddress, lastHour);
			File file = new File(outbox, path);
			if (!file.exists()) {
				return;
			}

			MessageProducer cat = Cat.getProducer();
			String ts = new SimpleDateFormat("mmss").format(new Date());
			Transaction root = cat.newTransaction("Task", "DumpRemoteIds-" + ipAddress + "-" + ts);

			root.addData("file", file);
			root.setStatus(Message.SUCCESS);

			Transaction t = cat.newTransaction("Task", "UploadRemoteIds");

			t.addData("file", path);

			try {
				FileInputStream fis = new FileInputStream(file);
				FSDataOutputStream fdos = makeHdfsOutputStream(path);

				long start = System.currentTimeMillis();

				m_logger.info(String.format("Start uploading(%s) to HDFS(%s) ...", file.getCanonicalPath(), path));
				Files.forIO().copy(fis, fdos, AutoClose.INPUT_OUTPUT);

				double sec = (System.currentTimeMillis() - start) / 1000d;
				String size = Formats.forNumber().format(file.length(), "0.#", "B");
				String speed = sec <= 0 ? "N/A" : Formats.forNumber().format(file.length() / sec, "0.0", "B/s");

				t.addData("size", size);
				t.addData("speed", speed);
				t.setStatus(Message.SUCCESS);

				m_logger.info(String.format("Finish remoteIds uploading(%s) to HDFS(%s) with size(%s) at %s.",
				      file.getCanonicalPath(), path, size, speed));

				if (!file.delete()) {
					m_logger.warn("Can't delete file: " + file);
				}
			} catch (AccessControlException e) {
				cat.logError(e);
				t.setStatus(e);
				m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
			} catch (Exception e) {
				cat.logError(e);
				t.setStatus(e);
				m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", file, path), e);
			} finally {
				t.complete();
			}

			root.complete();
			Cat.reset();
		}
	}
}
