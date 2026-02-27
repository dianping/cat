package com.dianping.cat.message.codec;

import com.dianping.cat.message.MetricBag;

import io.netty.buffer.ByteBuf;

public interface MetricBagDecoder {
	public MetricBag decode(ByteBuf buf);
}
