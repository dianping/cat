package com.dianping.cat.analysis;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.CatConstants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public final class TcpSocketReceiver implements LogEnabled {

	@Inject(type = MessageCodec.class, value = PlainTextMessageCodec.ID)
	private MessageCodec m_codec;

	@Inject
	private MessageHandler m_handler;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private ChannelFuture m_future;

	private EventLoopGroup m_bossGroup;

	private EventLoopGroup m_workerGroup;

	private Logger m_logger;

	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	private volatile long m_processCount;

	public synchronized void destory() {
		try {
			m_logger.info("start shutdown socket, port " + m_port);
			m_future.channel().closeFuture();
			m_bossGroup.shutdownGracefully();
			m_workerGroup.shutdownGracefully();
			m_logger.info("shutdown socket success");
		} catch (Exception e) {
			m_logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	protected boolean getOSMatches(String osNamePrefix) {
		String os = System.getProperty("os.name");

		if (os == null) {
			return false;
		}
		return os.startsWith(osNamePrefix);
	}

	public void init() {
		try {
			startServer(m_port);
		} catch (Throwable e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	public synchronized void startServer(int port) throws InterruptedException {
		boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
		int threads = 24;
		ServerBootstrap bootstrap = new ServerBootstrap();

		m_bossGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		m_workerGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		bootstrap.group(m_bossGroup, m_workerGroup);
		bootstrap.channel(linux ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				pipeline.addLast("decode", new MessageDecoder());
			}
		});

		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		try {
			m_future = bootstrap.bind(port).sync();
			m_logger.info("start netty server!");
		} catch (Exception e) {
			m_logger.error("Started Netty Server Failed:" + port, e);
		}
	}

	public class MessageDecoder extends ByteToMessageDecoder {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
			if (buffer.readableBytes() < 4) {
				return;
			}
			buffer.markReaderIndex();
			int length = buffer.readInt();
			buffer.resetReaderIndex();
			if (buffer.readableBytes() < length + 4) {
				return;
			}
			try {
				if (length > 0) {
					ByteBuf readBytes = buffer.readBytes(length + 4);
					readBytes.markReaderIndex();
					readBytes.readInt();

					DefaultMessageTree tree = (DefaultMessageTree) m_codec.decode(readBytes);

					readBytes.resetReaderIndex();
					tree.setBuffer(readBytes);
					m_handler.handle(tree);
					m_processCount++;

					long flag = m_processCount % CatConstants.SUCCESS_COUNT;

					if (flag == 0) {
						m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
					}
				} else {
					// client message is error
					buffer.readBytes(length);
				}
			} catch (Exception e) {
				m_serverStateManager.addMessageTotalLoss(1);
				m_logger.error(e.getMessage(), e);
			}
		}
	}

}