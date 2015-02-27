package com.dianping.cat.message.spi.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
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

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public final class TcpSocketReceiver implements LogEnabled {

	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessageHandler m_handler;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private DomainValidator m_domainValidator;

	private Logger m_logger;

	private int m_port = 2280; // default port number from phone, C:2, A:2, T:8

	private volatile long m_processCount;

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
		} catch (InterruptedException e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	public synchronized void startServer(int port) throws InterruptedException {
		boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
		int threads = 24;
		EventLoopGroup bossGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		EventLoopGroup workerGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
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
			bootstrap.bind(port).sync();
		} catch (Exception e) {
			Cat.logError("Started Netty Server Failed:" + port, e);
		}
		m_logger.info("start netty server!");
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
					boolean valid = m_domainValidator.validate(tree.getDomain());

					if (valid) {
						readBytes.resetReaderIndex();
						tree.setBuffer(readBytes);
						m_handler.handle(tree);
						m_processCount++;

						long flag = m_processCount % CatConstants.SUCCESS_COUNT;

						if (flag == 0) {
							m_serverStateManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
						}
					} else {
						m_logger.info("Invalid domain in TcpSocketReceiver found: " + tree.getDomain());
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