package com.dianping.cat.message.pipeline;

public interface MessageHandler {
	int getOrder();

	void handleMessage(MessageHandlerContext ctx, Object msg);
}
