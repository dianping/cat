package com.dianping.cat.consumer.dump;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.site.helper.Files;
import com.site.helper.Files.AutoClose;
import com.site.helper.Scanners;
import com.site.helper.Scanners.FileMatcher;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

/**
 * Dump message to hdfs
 * 
 * @author sean.wang
 * @since Apr 10, 2012
 */
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
			m_job = Threads.forGroup().start(new WriteJob());
		}
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
			try {
				while (isActive()) {
					upload();

					Thread.sleep(1000);
				}

			} catch (Exception e) {
				m_logger.warn("Error when dumping message to HDFS.", e);
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

			if (paths.size() > 0) {
				for (String path : paths) {
					File file = new File(baseDir, path);

					try {
						m_logger.info(String.format("Start uploading(%s) to HDFS(%s) ...", file.getCanonicalPath(), path));

						FileInputStream fis = new FileInputStream(file);
						FSDataOutputStream fdos = makeHdfsOutputStream(path);

						Files.forIO().copy(fis, fdos, AutoClose.INPUT_OUTPUT);

						if (!file.delete()) {
							m_logger.warn("Can't delete file: " + file);
						}

						m_logger.info(String.format("Finish uploading(%s) to HDFS(%s).", file.getCanonicalPath(), path));
					} catch (AccessControlException e) {
						m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
					} catch (IOException e) {
						m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", file, path), e);
					}
				}
			}
		}
	}
}
