package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.message.internal.ByteBufQueue;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

import io.netty.buffer.ByteBuf;

// Component
public class MessageConveyer extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "message-conveyer";

	// Inject
	private ByteBufQueue m_queue;

	@Override
	public int getOrder() {
		return 400;
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (msg instanceof ByteBuf) {
			m_queue.offer((ByteBuf) msg);
		} else {
			ctx.fireMessage(msg);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_queue = ctx.lookup(ByteBufQueue.class);
	}
}
