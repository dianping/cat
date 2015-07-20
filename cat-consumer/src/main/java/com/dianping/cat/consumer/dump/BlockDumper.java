package com.dianping.cat.consumer.dump;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
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
	      ServerStatisticManager stateManager) {
		int thread = 3;

		m_buckets = buckets;
		m_messageBlocks = messageBlock;
		m_serverStateManager = stateManager;
		m_executors = new ThreadPoolExecutor(thread, thread, 10, TimeUnit.SECONDS,
		      new LinkedBlockingQueue<Runnable>(5000), new ThreadPoolExecutor.CallerRunsPolicy());
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
}