package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TcpSocketSender implements Task, MessageSender, LogEnabled {
	public static final String ID = "tcp-socket-sender";

	private static final int MAX_CHILD_NUMBER = 200;

	private static final long HOUR = 1000 * 60 * 60L;

	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessageStatistics m_statistics;

	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private MessageIdFactory m_factory;

	private MessageQueue m_queue;

	private MessageQueue m_atomicTrees;

	private List<InetSocketAddress> m_serverAddresses;

	private ChannelManager m_manager;

	private Logger m_logger;

	private transient boolean m_active;

	private AtomicInteger m_errors = new AtomicInteger();

	private AtomicInteger m_attempts = new AtomicInteger();

	public static int getQueueSize() {
		String size = System.getProperty("queue.size", "1000");

		return Integer.parseInt(size);
	}

	private boolean checkWritable(ChannelFuture future) throws InterruptedException {
		boolean isWriteable = false;
		Channel channel = future.channel();

		if (channel.isOpen()) {
			if (channel.isActive() && channel.isWritable()) {
				isWriteable = true;
			} else {
				int count = m_attempts.incrementAndGet();

				if (count % 1000 == 0 || count == 1) {
					m_logger.warn("Netty write buffer is full! Attempts: " + count);
				}

				TimeUnit.MILLISECONDS.sleep(5);
			}
		}

		return isWriteable;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "TcpSocketSender";
	}

	@Override
	public void initialize() {
		int len = getQueueSize();

		m_queue = new DefaultMessageQueue(len);
		m_atomicTrees = new DefaultMessageQueue(len);

		m_manager = new ChannelManager(m_logger, m_serverAddresses, m_queue, m_configManager, m_factory);

		Threads.forGroup("cat").start(this);
		Threads.forGroup("cat").start(m_manager);
		Threads.forGroup("cat").start(new MergeAtomicTask());
	}

	private boolean isAtomicMessage(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String type = message.getType();

			if (type.startsWith("Cache.") || "SQL".equals(type)) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	private void logQueueFullInfo(MessageTree tree) {
		if (m_statistics != null) {
			m_statistics.onOverflowed(tree);
		}

		int count = m_errors.incrementAndGet();

		if (count % 1000 == 0 || count == 1) {
			m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
		}

		tree = null;
	}

	private MessageTree mergeTree(MessageQueue trees) {
		int max = MAX_CHILD_NUMBER;
		DefaultTransaction t = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
		MessageTree first = trees.poll();

		t.setStatus(Transaction.SUCCESS);
		t.setCompleted(true);
		t.addChild(first.getMessage());
		t.setTimestamp(first.getMessage().getTimestamp());
		long lastTimestamp = 0;
		long lastDuration = 0;

		while (max >= 0) {
			MessageTree tree = trees.poll();

			if (tree == null) {
				t.setDurationInMillis(lastTimestamp - t.getTimestamp() + lastDuration);
				break;
			}
			lastTimestamp = tree.getMessage().getTimestamp();
			if (tree.getMessage() instanceof DefaultTransaction) {
				lastDuration = ((DefaultTransaction) tree.getMessage()).getDurationInMillis();
			} else {
				lastDuration = 0;
			}
			t.addChild(tree.getMessage());
			m_factory.reuse(tree.getMessageId());
			max--;
		}

		((DefaultMessageTree) first).setMessage(t);
		return first;
	}

	@Override
	public void run() {
		m_active = true;

		try {
			while (m_active) {
				ChannelFuture channel = m_manager.channel();

				if (channel != null && checkWritable(channel)) {
					try {
						MessageTree tree = m_queue.poll();

						if (tree != null) {
							sendInternal(tree);
							tree.setMessage(null);
						}

					} catch (Throwable t) {
						m_logger.error("Error when sending message over TCP socket!", t);
					}
				} else {
					long current = System.currentTimeMillis();
					long oldTimestamp = current - HOUR;

					while (true) {
						try {
							MessageTree tree = m_queue.peek();

							if (tree != null && tree.getMessage().getTimestamp() < oldTimestamp) {
								MessageTree discradTree = m_queue.poll();

								if (discradTree != null) {
									m_statistics.onOverflowed(discradTree);
								}
							} else {
								break;
							}
						} catch (Exception e) {
							m_logger.error(e.getMessage(), e);
							break;
						}
					}

					TimeUnit.MILLISECONDS.sleep(5);
				}
			}
		} catch (InterruptedException e) {
			// ignore it
			m_active = false;
		}
	}

	@Override
	public void send(MessageTree tree) {
		if (isAtomicMessage(tree)) {
			boolean result = m_atomicTrees.offer(tree, m_manager.getSample());

			if (!result) {
				logQueueFullInfo(tree);
			}
		} else {
			boolean result = m_queue.offer(tree, m_manager.getSample());

			if (!result) {
				logQueueFullInfo(tree);
			}
		}
	}

	private void sendInternal(MessageTree tree) {
		ChannelFuture future = m_manager.channel();
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(10 * 1024); // 10K

		m_codec.encode(tree, buf);

		int size = buf.readableBytes();
		Channel channel = future.channel();

		channel.writeAndFlush(buf);
		if (m_statistics != null) {
			m_statistics.onBytes(size);
		}
	}

	public void setServerAddresses(List<InetSocketAddress> serverAddresses) {
		m_serverAddresses = serverAddresses;
	}

	// merge atomic messages for 30 seconds or 200 messages
	private boolean shouldMerge(MessageQueue trees) {
		MessageTree tree = trees.peek();

		if (tree != null) {
			long firstTime = tree.getMessage().getTimestamp();
			int maxDuration = 1000 * 30;

			if (System.currentTimeMillis() - firstTime > maxDuration || trees.size() >= MAX_CHILD_NUMBER) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void shutdown() {
		m_active = false;
		m_manager.shutdown();
	}

	public class MergeAtomicTask implements Task {

		@Override
		public String getName() {
			return "merge-atomic-task";
		}

		@Override
		public void run() {
			while (true) {
				if (shouldMerge(m_atomicTrees)) {
					MessageTree tree = mergeTree(m_atomicTrees);
					boolean result = m_queue.offer(tree);

					if (!result) {
						logQueueFullInfo(tree);
					}
				} else {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
