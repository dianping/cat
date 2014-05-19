package com.dianping.cat.message.spi.core;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public class TcpSocketReceiver implements LogEnabled {
	private boolean m_active = false;

	private ChannelGroup m_channelGroup = new DefaultChannelGroup();

	private ChannelFactory m_factory;

	@Inject
	private String m_host;

	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessageHandler m_handler;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private Logger m_logger;

	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	private int m_queueSize = 10000;

	private volatile int m_errorCount;

	private volatile long m_processCount;

	private volatile int m_decodeThreads = 15;

	private ConcurrentHashMap<Integer, LinkedBlockingQueue<ChannelBuffer>> m_queues = new ConcurrentHashMap<Integer, LinkedBlockingQueue<ChannelBuffer>>();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void init() {
		if (m_serverConfigManager.isLocalMode()) {
			m_decodeThreads = 1;
		}
		startEncoderThreads(m_decodeThreads);

		ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

		InetSocketAddress address;

		m_host = m_serverConfigManager.getBindHost();
		m_port = m_serverConfigManager.getBindPort();

		if (m_host == null) {
			address = new InetSocketAddress(m_port);
		} else {
			address = new InetSocketAddress(m_host, m_port);
		}

		ExecutorService bossExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Boss-" + address);
		ExecutorService workerExecutor = Threads.forPool().getCachedThreadPool("Cat-TcpSocketReceiver-Worker");
		ChannelFactory factory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() {
				return Channels.pipeline(new MessageDecoder(), new MessageTreeHandler());
			}
		});

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.bind(address);

		m_logger.info("CAT server started at " + address);
		m_factory = factory;
		m_active = true;
	}

	public boolean isActive() {
		return m_active;
	}

	public void setQueueSize(int queueSize) {
		m_queueSize = queueSize;
	}

	public void startEncoderThreads(int threadSize) {
		for (int i = 0; i < threadSize; i++) {
			LinkedBlockingQueue<ChannelBuffer> queue = new LinkedBlockingQueue<ChannelBuffer>(m_queueSize);
			DecodeMessageTask messageDecoder = new DecodeMessageTask(i, queue, m_codec, m_handler);
			Threads.forGroup("Cat").start(messageDecoder);

			m_queues.put(i, queue);
		}
	}

	public class DecodeMessageTask implements Task {

		private int m_index;

		private BlockingQueue<ChannelBuffer> m_queue;

		private long m_count = -1;

		public DecodeMessageTask(int index, BlockingQueue<ChannelBuffer> queue, MessageCodec codec, MessageHandler handler) {
			m_index = index;
			m_queue = queue;
			m_codec = codec;
			m_handler = handler;
		}

		private void decodeMessage(ChannelBuffer buf) {
			try {
				buf.markReaderIndex();

				// read the size of the message
				buf.readInt();
				DefaultMessageTree tree = (DefaultMessageTree) m_codec.decode(buf);
				buf.resetReaderIndex();
				tree.setBuffer(buf);
				m_handler.handle(tree);
			} catch (Throwable e) {
				buf.resetReaderIndex();

				String raw = buf.toString(0, buf.readableBytes(), Charset.forName("utf-8"));
				m_logger.error("Error when handling message! Raw buffer: " + raw, e);
			}
		}

		private void decodeMessageWithMonitor(ChannelBuffer buf) {
			Transaction t = Cat.newTransaction("Decode", "Thread-" + m_index);

			decodeMessage(buf);

			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}

		@Override
		public String getName() {
			return "Message-Decode-" + m_index;
		}

		public void handleMessage() throws InterruptedException {
			ChannelBuffer buf = m_queue.poll(1, TimeUnit.MILLISECONDS);

			if (buf != null) {
				m_count++;

				if (m_count % 10000 == 0) {
					decodeMessageWithMonitor(buf);
				} else {
					decodeMessage(buf);
				}
			}
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					handleMessage();
				} catch (InterruptedException e) {
					active = false;
				} catch (Exception e) {
					// ignore
				}
			}
			try {
				if (m_index == 1) {
					ChannelGroupFuture future = m_channelGroup.close();

					future.awaitUninterruptibly();
					m_factory.releaseExternalResources();
				}

			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		}

		@Override
		public void shutdown() {
		}

	}

	public static class MessageDecoder extends FrameDecoder {
		@Override
		/**
		 * return null means not all data is ready, so waiting for next network package.
		 */
		protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
			if (buffer.readableBytes() < 4) {
				return null;
			}

			buffer.markReaderIndex();

			int length = buffer.readInt();

			buffer.resetReaderIndex();

			if (buffer.readableBytes() < length + 4) {
				return null;
			}

			return buffer.readBytes(length + 4);
		}
	}

	class MessageTreeHandler extends SimpleChannelHandler {

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
			m_channelGroup.add(event.getChannel());
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) {
			m_logger.warn(event.getChannel().toString(), event.getCause());

			event.getChannel().close();
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
			m_processCount++;

			ChannelBuffer buf = (ChannelBuffer) event.getMessage();

			int index = (int) (m_processCount % m_decodeThreads);
			int retryTime = 0;
			boolean errorFlag = true;

			while (retryTime < m_decodeThreads) {
				LinkedBlockingQueue<ChannelBuffer> queue = m_queues.get((index + retryTime) % m_decodeThreads);
				boolean result = queue.offer(buf);

				if (result) {
					errorFlag = false;
					break;
				}
				retryTime++;
			}

			if (errorFlag) {
				m_errorCount++;
				if (m_errorCount % CatConstants.ERROR_COUNT == 0) {
					m_serverStateManager.addMessageTotalLoss(CatConstants.ERROR_COUNT);

				}
			}
			long flag = m_processCount % CatConstants.SUCCESS_COUNT;

			if (flag == 0) {
				m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
			}
		}
	}

}
