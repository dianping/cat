package com.dianping.cat.message.tree;

import io.netty.buffer.ByteBuf;

public interface ByteBufQueue {
	boolean offer(ByteBuf buf);

	ByteBuf poll();
}
