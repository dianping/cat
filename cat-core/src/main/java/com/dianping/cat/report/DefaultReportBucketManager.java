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
package com.dianping.cat.report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.FileMatcher;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

@Named(type = ReportBucketManager.class)
public class DefaultReportBucketManager extends ContainerHolder implements ReportBucketManager, Initializable {

	@Inject
	private ServerConfigManager m_configManager;

	private String m_reportBaseDir;

	@Override
	public void clearOldReports() {
		Transaction t = Cat.newTransaction("System", "DeleteReport");
		try {
			File reportDir = new File(m_reportBaseDir);
			final List<String> toRemovePaths = new ArrayList<String>();
			final Set<String> validPaths = queryValidPath(m_configManager.getLocalReportStroageTime());

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
					for (String str : validPaths) {
						if (path.contains(str)) {
							return false;
						}
					}
					return true;
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

	@Override
	public void closeBucket(ReportBucket bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		} finally {
			release(bucket);
		}
	}

	@Override
	public ReportBucket getReportBucket(long timestamp, String name, int index) throws IOException {
		Date date = new Date(timestamp);
		ReportBucket bucket = lookup(ReportBucket.class);

		bucket.initialize(name, date, index);
		return bucket;
	}

	@Override
	public void initialize() throws InitializationException {
		m_reportBaseDir = Cat.getCatHome() + "bucket/report";
	}

	private Set<String> queryValidPath(int day) {
		Set<String> strs = new HashSet<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		long currentTimeMillis = System.currentTimeMillis();

		for (int i = 0; i < day; i++) {
			Date date = new Date(currentTimeMillis - i * 24 * 60 * 60 * 1000L);

			strs.add(sdf.format(date));
		}
		return strs;
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

}
