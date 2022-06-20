package com.dianping.cat.message.encoder;

import com.dianping.cat.message.internal.MetricBag;

import io.netty.buffer.ByteBuf;

public interface MetricBagEncoder {
	public void encode(MetricBag bag, ByteBuf buf);
}
