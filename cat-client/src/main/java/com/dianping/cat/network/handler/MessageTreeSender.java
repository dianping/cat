package com.dianping.cat.network.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.message.io.MessageTreePool;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.util.Threads.Task;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class MessageTreeSender extends ChannelInboundHandlerAdapter implements Initializable, LogEnabled, Task {
	// Inject
	private MessageTreePool m_pool;

	private List<Channel> m_channels = new CopyOnWriteArrayList<>();

	private MessageTree m_tree;

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	private CountDownLatch m_latch = new CountDownLatch(1);

	private Logger m_logger;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();

		m_channels.add(channel);
		m_logger.info("Connected to CAT server %s, %s", channel.remoteAddress(), channel);

		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();

		m_channels.remove(channel);
		m_logger.info("Disconnected from CAT server %s, %s", channel.remoteAddress(), channel);

		super.channelInactive(ctx);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Channel> getActiveChannels() {
		return m_channels;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_pool = ctx.lookup(MessageTreePool.class);
	}

	private MessageTree next() throws InterruptedException {
		if (m_tree == null) {
			m_tree = m_pool.poll();
		}

		return m_tree;
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				MessageTree tree = next();

				if (tree != null) {
					boolean success = write(tree);

					if (success) {
						m_tree = null;
						continue;
					}
				}

				TimeUnit.MILLISECONDS.sleep(5);
			}

			// if shutdown in progress
			if (!m_enabled.get()) {
				MessageTree tree = next();

				while (tree != null) {
					boolean success = write(tree);

					if (success) {
						tree = next();
					} else {
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		} finally {
			m_latch.countDown();
		}
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

	private boolean write(MessageTree tree) {
		if (!m_channels.isEmpty()) {
			Channel channel = m_channels.get(0);

			if (channel.isActive() && channel.isWritable()) {
				channel.writeAndFlush(tree);
				return true;
			}
		}

		return false;
	}
}
