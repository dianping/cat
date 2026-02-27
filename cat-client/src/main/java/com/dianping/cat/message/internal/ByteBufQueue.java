package com.dianping.cat.message.internal;

import io.netty.buffer.ByteBuf;

public interface ByteBufQueue {
	boolean offer(ByteBuf buf);

	ByteBuf poll();
}
