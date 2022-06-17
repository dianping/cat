package com.dianping.cat.message.pipeline;

import com.dianping.cat.message.Log;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.tree.MessageTree;

public class MessageHandlerAdaptor implements MessageHandler {
	@Override
	public int getOrder() {
		return 0;
	}

	protected void handleLog(MessageHandlerContext ctx, Log log) {
		ctx.fireMessage(log);
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (msg instanceof MessageTree) {
			handleMessagreTree(ctx, (MessageTree) msg);
		} else if (msg instanceof Log) {
			handleLog(ctx, (Log) msg);
		} else if (msg instanceof Metric) {
			handleMetric(ctx, (Metric) msg);
		} else {
			ctx.fireMessage(msg);
		}
	}

	protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		ctx.fireMessage(tree);
	}

	protected void handleMetric(MessageHandlerContext ctx, Metric metric) {
		ctx.fireMessage(metric);
	}
}
