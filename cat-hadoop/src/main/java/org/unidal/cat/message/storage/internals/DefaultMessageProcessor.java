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
package org.unidal.cat.message.storage.internals;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.MessageFinder;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageProcessor.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageProcessor implements MessageProcessor, MessageFinder {
	@Inject
	private BlockDumperManager m_blockDumperManager;

	@Inject
	private MessageFinderManager m_finderManager;

	@Inject
	private ServerConfigManager m_configManger;

	private BlockDumper m_dumper;

	private int m_index;

	private BlockingQueue<MessageTree> m_queue;

	private ConcurrentHashMap<String, Block> m_blocks = new ConcurrentHashMap<String, Block>();

	private int m_hour;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	private int m_count;

	@Override
	public ByteBuf find(MessageId id) {
		String domain = id.getDomain();
		Block block = m_blocks.get(domain);

		if (block != null) {
			return block.find(id);
		}

		return null;
	}

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return getClass().getSimpleName() + " " + sdf.format(new Date(TimeUnit.HOURS.toMillis(m_hour))) + "-" + m_index;
	}

	@Override
	public void initialize(int hour, int index, BlockingQueue<MessageTree> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_dumper = m_blockDumperManager.findOrCreate(hour);
		m_hour = hour;
		m_latch = new CountDownLatch(1);
		m_finderManager.register(hour, this);
	}

	private boolean isMonitor() {
		return (++m_count) % 100000 == 0;
	}

	private MessageTree pollMessage() throws InterruptedException {
		return m_queue.poll(5, TimeUnit.MILLISECONDS);
	}

	private void processMessage(MessageTree tree) {
		MessageId id = tree.getFormatMessageId();
		String domain = id.getDomain();
		int hour = id.getHour();
		Block block = m_blocks.get(domain);

		if (block == null) {
			block = new DefaultBlock(domain, hour);
			m_blocks.put(domain, block);
		}

		ByteBuf buffer = tree.getBuffer();

		try {
			if (block.isFull()) {
				block.finish();

				m_dumper.dump(block);

				block = new DefaultBlock(domain, hour);
				m_blocks.put(domain, block);
			}

			block.pack(id, buffer);
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			ReferenceCountUtil.release(buffer);
		}
	}

	@Override
	public void run() {
		MessageTree tree;

		try {
			while (m_enabled.get() || !m_queue.isEmpty()) {
				tree = pollMessage();

				if (tree != null) {
					if (isMonitor()) {
						Transaction t = Cat.newTransaction("Processor", "index-" + m_index);

						processMessage(tree);
						t.setStatus(Transaction.SUCCESS);
						t.complete();
					} else {
						processMessage(tree);
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		// Cat.logEvent("BlockSize", String.valueOf(m_blocks.size()),
		// Event.SUCCESS, m_blocks.keySet().toString());

		for (Block block : m_blocks.values()) {
			try {
				block.finish();

				m_dumper.dump(block);
			} catch (IOException e) {
				// ignore it
			}
		}

		m_blocks.clear();
		m_latch.countDown();
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}
	}
}
