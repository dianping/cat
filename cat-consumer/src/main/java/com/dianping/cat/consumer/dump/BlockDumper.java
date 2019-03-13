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
package com.dianping.cat.consumer.dump;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.message.storage.MessageBlock;
import com.dianping.cat.statistic.ServerStatisticManager;

public class BlockDumper implements Task {
	private int m_errors;

	private ConcurrentHashMap<String, LocalMessageBucket> m_buckets;

	private BlockingQueue<MessageBlock> m_messageBlocks;

	private ServerStatisticManager m_serverStateManager;

	private ThreadPoolExecutor m_executors;

	public BlockDumper(ConcurrentHashMap<String, LocalMessageBucket> buckets, BlockingQueue<MessageBlock> messageBlock,
							ServerStatisticManager stateManager, ServerConfigManager configManager) {
		int thread = configManager.getBlockDumpThread();

		m_buckets = buckets;
		m_messageBlocks = messageBlock;
		m_serverStateManager = stateManager;
		m_executors = new ThreadPoolExecutor(thread, thread, 10, TimeUnit.SECONDS,	new ArrayBlockingQueue<Runnable>(5000),
								new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Override
	public String getName() {
		return "LocalMessageBucketManager-BlockDumper";
	}

	@Override
	public void run() {
		try {
			while (true) {
				MessageBlock block = m_messageBlocks.poll(5, TimeUnit.MILLISECONDS);

				if (block != null) {
					m_executors.submit(new FlushBlockTask(block));
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	private void flushBlock(MessageBlock block) {
		long time = System.currentTimeMillis();
		String dataFile = block.getDataFile();
		LocalMessageBucket bucket = m_buckets.get(dataFile);

		try {
			bucket.getWriter().writeBlock(block);
		} catch (Throwable e) {
			m_errors++;

			if (m_errors == 1 || m_errors % 100 == 0) {
				Cat.logError(new RuntimeException("Error when dumping for bucket: " + dataFile + ".", e));
			}
		}
		m_serverStateManager.addBlockTotal(1);
		long duration = System.currentTimeMillis() - time;
		m_serverStateManager.addBlockTime(duration);
	}

	@Override
	public void shutdown() {
	}

	public class FlushBlockTask implements Task {

		private MessageBlock m_block;

		public FlushBlockTask(MessageBlock block) {
			m_block = block;
		}

		@Override
		public void run() {
			flushBlock(m_block);
		}

		@Override
		public String getName() {
			return "flush-block";
		}

		@Override
		public void shutdown() {
		}

	}
}