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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named
public class LogviewProcessor implements Task, Initializable {

	@Inject
	private HdfsUploader m_hdfsUploader;

	@Inject
	private ServerConfigManager m_configManager;

	private File m_baseDir;

	private void deleteLocalFile(String path) {
		File file = new File(m_baseDir, path);
		File parent = file.getParentFile();

		file.delete();
		parent.delete(); // delete it if empty
		parent.getParentFile().delete(); // delete it if empty
	}

	private void deleteOldMessages() {
		final Set<String> paths = new HashSet<String>();
		final Set<String> validPaths = findValidPath(m_configManager.getLogViewStroageTime());

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (shouldDelete(path)) {
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
			processLogviewFiles(new ArrayList<String>(paths), false);
		}
	}

	public List<String> findOldBuckets() {
		final Set<String> paths = new HashSet<String>();

		Scanners.forDir().scan(m_baseDir, new FileMatcher() {
			@Override
			public Direction matches(File base, String path) {
				if (new File(base, path).isFile()) {
					if (isOldBucketFile(path)) {
						paths.add(path);
					}
				}
				return Direction.DOWN;
			}
		});
		return new ArrayList<String>(paths);
	}

	private Set<String> findValidPath(int storageDays) {
		Set<String> strs = new HashSet<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long currentTimeMillis = System.currentTimeMillis();

		for (int i = 0; i < storageDays; i++) {
			Date date = new Date(currentTimeMillis - i * TimeHelper.ONE_DAY);

			strs.add(sdf.format(date));
		}
		return strs;
	}

	@Override
	public String getName() {
		return "logview-processor";
	}

	@Override
	public void initialize() throws InitializationException {
		m_baseDir = new File(m_configManager.getHdfsLocalBaseDir("dump"));
	}

	private boolean isOldBucketFile(String path) {
		long current = System.currentTimeMillis();
		long currentHour = current - current % TimeHelper.ONE_HOUR;
		long lastHour = currentHour - TimeHelper.ONE_HOUR;
		long nextHour = currentHour + TimeHelper.ONE_HOUR;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/HH");

		int currentPos = path.indexOf(sdf.format(new Date(currentHour)));
		int lastPos = path.indexOf(sdf.format(new Date(lastHour)));
		int nextPos = path.indexOf(sdf.format(new Date(nextHour)));

		if (currentPos > -1 || lastPos > -1 || nextPos > -1) {
			return false;
		}
		return true;
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
					uploadFileToHdfs(path);
					Cat.getProducer().logEvent("Upload", "UploadAndDelete", Message.SUCCESS, loginfo);
				} else {
					deleteLocalFile(path);
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
			long start = System.currentTimeMillis();
			long current = start / 1000 / 60;
			int min = (int) (current % (60));
			Calendar nextStart = Calendar.getInstance();

			nextStart.set(Calendar.MINUTE, 10);
			nextStart.add(Calendar.HOUR, 1);
			try {
				if (m_configManager.isHdfsOn()) {
					// make system 0-10 min is not busy
					if (min >= 9) {
						List<String> paths = findOldBuckets();

						processLogviewFiles(paths, true);
					}
				} else {
					// for clean java memory
					deleteOldMessages();
				}
			} catch (Throwable e) {
				Cat.logError(e);
			}
			try {
				long end = System.currentTimeMillis();
				long sleepTime = nextStart.getTimeInMillis() - end;

				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

	private void uploadFileToHdfs(String path) {
		File file = new File(m_baseDir, path);

		m_hdfsUploader.uploadLogviewFile(path, file);
	}

}