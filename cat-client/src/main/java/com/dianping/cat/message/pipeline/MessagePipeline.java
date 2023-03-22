package com.dianping.cat.message.pipeline;

public interface MessagePipeline extends MessageHandler {
	void addLast(MessageHandler handler);

	MessageHandlerContext headContext(Object message);
}
