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
package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.HarFileSystem;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;

public class HarConnectionPool implements Initializable {

	private ServerConfigManager m_serverConfigManager;

	private MessageFormat m_format = new MessageFormat("{0}/{1}/{2,date,yyyyMMdd}/{2,date,HH}.har");

	private Map<String, Pair<HarFileSystem, Long>> m_hars = new ConcurrentHashMap<String, Pair<HarFileSystem, Long>>();

	public HarConnectionPool(ServerConfigManager manager) {
		m_serverConfigManager = manager;
	}

	private void closeIdleHarfs() throws IOException {
		long now = System.currentTimeMillis();
		Set<String> closed = new HashSet<String>();

		for (Entry<String, Pair<HarFileSystem, Long>> entry : m_hars.entrySet()) {
			Pair<HarFileSystem, Long> pair = entry.getValue();

			if (now - pair.getValue() >= TimeHelper.ONE_HOUR) {
				try {
					closed.add(entry.getKey());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
		for (String close : closed) {
			m_hars.remove(close);
			Cat.logEvent("HarConnClose", close);
		}
	}

	public HarFileSystem getHarfsConnection(String id, Date date, FileSystem fs) throws IOException {
		String serverUri = m_serverConfigManager.getHarfsServerUri(id);
		String baseUri = m_serverConfigManager.getHarfsBaseDir(id);
		String harUri = m_format.format(new Object[] { serverUri, baseUri, date });
		Pair<HarFileSystem, Long> har = m_hars.get(harUri);
		long current = System.currentTimeMillis();

		if (har == null) {
			synchronized (this) {
				if (har == null) {

					URI uri = URI.create(harUri);
					HarFileSystem harfs = new HarFileSystem(fs);

					try {
						harfs.initialize(uri, harfs.getConf());
						har = new Pair<HarFileSystem, Long>(harfs, current);

						m_hars.put(harUri, har);
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		if (har != null) {
			har.setValue(current);
			return har.getKey();
		} else {
			return null;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(new IdleChecker());
	}

	class IdleChecker implements Task {
		@Override
		public String getName() {
			return "HarConnectionPool-IdleChecker";
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(60 * 1000L); // 1 minute

					try {
						closeIdleHarfs();
					} catch (IOException e) {
						Cat.logError(e);
					}
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
