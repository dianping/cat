package com.dianping.cat.hadoop.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.security.AccessControlException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.helper.Formats;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class HdfsUploader implements LogEnabled {

	@Inject
	private FileSystemManager m_fileSystemManager;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_fileSystemManager.getFileSystem(ServerConfigManager.DUMP_DIR, baseDir);
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

	public boolean uploadLogviewFile(String path, File file) {
		Transaction t = Cat.newTransaction("System", "UploadDump");
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
			return true;
		} catch (AlreadyBeingCreatedException e) {
			Cat.logError(e);
			t.setStatus(e);

			m_logger.error(String.format("Already being created (%s)!", path), e);
		} catch (AccessControlException e) {
			Cat.logError(e);
			t.setStatus(e);
			m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
		} catch (Exception e) {
			Cat.logError(e);
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
		return false;
	}
}
