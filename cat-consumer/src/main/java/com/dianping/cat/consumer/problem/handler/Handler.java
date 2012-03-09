package com.dianping.cat.consumer.problem.handler;

import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.spi.MessageTree;

public interface Handler {
	public int handle(Segment segment, MessageTree tree);
}