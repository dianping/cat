package com.dianping.cat.consumer.dump;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.site.helper.Scanners;
import com.site.helper.Scanners.IMatcher;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

/**
 * Dump message to hdfs
 * 
 * @author sean.wang
 * @since Apr 10, 2012
 */
public class DumpUploader extends ContainerHolder implements Initializable, LogEnabled {
	private static final int DEFAULT_CHECK_DURATION = 5 * 1000; // ms

	private String m_baseDir;

	@Inject
	private FileSystemManager m_fileSystemManager;

	private Logger m_logger;

	private Thread m_thread;

	private WriteJob m_job;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDir = m_fileSystemManager.getServerConfig().getStorage().getLocalBaseDir();
		m_job = new WriteJob();
		Thread thread = new Thread(m_job);
		thread.setName("MessageDumpToHdfs");
		thread.start();
		m_thread = thread;

	}

	public void dispose() {
		m_job.shutdown();
		try {
			m_thread.join();
		} catch (InterruptedException e) {
		}
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		String id = "dump";
		String key = id + ":" + path;
		FileSystem fs = m_fileSystemManager.getFileSystem(key, id, path, baseDir);
		Path file = new Path(baseDir.toString(), path);
		FSDataOutputStream out = fs.create(file);
		return out;
	}

	private void transfer(FileInputStream fis, FSDataOutputStream fdos) throws IOException {
		byte[] buffer = new byte[10 * 1024];
		int byteRead = -1;
		while ((byteRead = fis.read(buffer)) != -1) {
			fdos.write(buffer, 0, byteRead);
		}
		fdos.flush();
	}

	public void upload() {
		File baseDir = new File(m_baseDir);
		final List<String> paths = new ArrayList<String>();
		Scanners.forDir().scan(baseDir, new IMatcher<File>() {
			@Override
			public boolean isDirEligible() {
				return false;
			}

			@Override
			public boolean isFileElegible() {
				return true;
			}

			@Override
			public Direction matches(File base, String path) {
				paths.add(path);
				return Direction.NEXT;
			}
		});

		for (String path : paths) {
			File file = new File(baseDir, path);
			FSDataOutputStream fdos = null;
			FileInputStream fis = null;
			try {
				fdos = makeHdfsOutputStream(path);
				fis = new FileInputStream(file);
				transfer(fis, fdos);
			} catch (IOException e) {
				m_logger.error("transfer file to hdfs fail", e);
				continue;
			} finally {
				try {
					if (fdos != null) {
						fdos.close();
					}
				} catch (IOException e) {
				}
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
				}
			}
			file.delete();
		}

	}

	private class WriteJob implements Runnable {

		private volatile boolean m_active = true;

		private boolean isActive() {
			return m_active;
		}

		@Override
		public void run() {
			long lastCheckedTime = System.currentTimeMillis();

			try {
				while (isActive()) {
					upload();

					if (System.currentTimeMillis() - lastCheckedTime >= DEFAULT_CHECK_DURATION) {
						lastCheckedTime = System.currentTimeMillis();
					}
				}

			} catch (Exception e) {
				m_logger.warn("Error when dumping message to HDFS.", e);
			}

		}

		public void shutdown() {
			m_active = false;
		}
	}
}
