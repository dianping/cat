package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;
import com.dianping.cat.message.tree.ByteBufQueue;

import io.netty.buffer.ByteBuf;

// Component
public class MessageConveyer extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "conveyer";

	// Inject
	private ByteBufQueue m_queue;

	@Override
	public int getOrder() {
		return 1000;
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (msg instanceof ByteBuf) {
			m_queue.offer((ByteBuf) msg);
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_queue = ctx.lookup(ByteBufQueue.class);
	}
}
