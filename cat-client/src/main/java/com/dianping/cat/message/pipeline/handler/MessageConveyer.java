package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.message.internal.ByteBufQueue;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;
import com.dianping.cat.status.MessageStatistics;

import io.netty.buffer.ByteBuf;

// Component
public class MessageConveyer extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "message-conveyer";

	// Inject
	private ByteBufQueue m_queue;

	// Inject
	private MessageStatistics m_statistics;

	@Override
	public int getOrder() {
		return 400;
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) msg;

			m_statistics.onBytes(buf.readableBytes());

			if (!m_queue.offer(buf)) {
				m_statistics.onOverflowed();
			}
		} else {
			ctx.fireMessage(msg);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_queue = ctx.lookup(ByteBufQueue.class);
		m_statistics = ctx.lookup(MessageStatistics.class);
	}
}
