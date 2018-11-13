/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unidal.cat.message.storage.clean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.helper.Formats;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named
public class HdfsUploader implements LogEnabled, Initializable {

	@Inject
	private HdfsSystemManager m_fileSystemManager;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	private ThreadPoolExecutor m_executors;

	private File m_localBaseDir;

	private Logger m_logger;

	private void deleteFile(String path) {
		File file = new File(m_localBaseDir, path);
		File parent = file.getParentFile();

		file.delete();
		parent.delete();
		parent.getParentFile().delete();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		int thread = m_serverConfigManager.getHdfsUploadThreadsCount();

		m_localBaseDir = new File(m_serverConfigManager.getHdfsLocalBaseDir(HdfsSystemManager.DUMP));
		m_executors = new ThreadPoolExecutor(thread, thread, 10, TimeUnit.SECONDS,	new ArrayBlockingQueue<Runnable>(5000),
								new ThreadPoolExecutor.CallerRunsPolicy());
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		FileSystem fs = m_fileSystemManager.getFileSystem();
		String baseDir = m_fileSystemManager.getBaseDir();
		Path file = new Path(baseDir, path);
		FSDataOutputStream out;

		try {
			out = fs.create(file, true);
		} catch (RemoteException re) {
			fs.delete(file, false);

			out = fs.create(file);
		} catch (AlreadyBeingCreatedException e) {
			fs.delete(file, false);

			out = fs.create(file);
		}
		return out;
	}

	public boolean upload(String path, File file) {
		if (file.exists()) {
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

				deleteFile(path);
				return true;
			} catch (AlreadyBeingCreatedException e) {
				Cat.logError(e);
				t.setStatus(e);

				deleteFile(path);
				m_logger.error(String.format("Already being created (%s)!", path), e);
			} catch (AccessControlException e) {
				Cat.logError(e);
				t.setStatus(e);

				deleteFile(path);
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
				} catch (Exception e) {
					Cat.logError(e);
				} finally {
					t.complete();
				}
			}
		}
		return false;
	}

	public void uploadLogviewFile(String path, File file) {
		try {
			m_executors.submit(new Uploader(path, file));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public class Uploader implements Task {

		private String m_path;

		private File m_file;

		public Uploader(String path, File file) {
			m_path = path;
			m_file = file;
		}

		@Override
		public String getName() {
			return "hdfs-uploader";
		}

		@Override
		public void run() {
			upload(m_path, m_file);
		}

		@Override
		public void shutdown() {
		}
	}

}
