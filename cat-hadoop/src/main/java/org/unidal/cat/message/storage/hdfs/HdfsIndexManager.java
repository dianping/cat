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
package org.unidal.cat.message.storage.hdfs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Index;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;

@Named
public class HdfsIndexManager extends ContainerHolder implements Initializable, LogEnabled {

	protected Logger m_logger;

	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private HdfsSystemManager m_fileSystemManager;

	@Inject(value = "hdfs")
	private MessageConsumerFinder m_consumerFinder;

	private Map<String, HdfsIndex> m_buckets = new LinkedHashMap<String, HdfsIndex>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, HdfsIndex> eldest) {
			return size() > 1000;
		}
	};

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public MessageId loadMessage(MessageId id) {
		if (m_configManager.isHdfsOn()) {
			Transaction t = Cat.newTransaction("Hdfs", getClass().getSimpleName());
			t.setStatus(Message.SUCCESS);

			try {
				Set<String> ips = m_consumerFinder.findConsumerIps(id.getDomain(), id.getHour());

				t.addData(ips.toString());

				return readMessage(id, ips);
			} catch (RuntimeException e) {
				t.setStatus(e);
				Cat.logError(e);
				throw e;
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
		return null;
	}

	private MessageId readMessage(MessageId id, Set<String> ips) {
		for (String ip : ips) {
			String domain = id.getDomain();
			int hour = id.getHour();
			String key = domain + '-' + ip + '-' + hour;

			try {
				HdfsIndex bucket = m_buckets.get(key);

				if (bucket == null) {
					synchronized (m_buckets) {
						bucket = m_buckets.get(key);

						if (bucket == null) {
							bucket = (HdfsIndex) lookup(Index.class, HdfsIndex.ID);

							bucket.initialize(domain, ip, hour);
							m_buckets.put(key, bucket);

							super.release(bucket);
						}
					}
				}

				if (bucket != null) {
					MessageId to = bucket.find(id);

					if (to != null) {
						return to;
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return null;
	}

}
