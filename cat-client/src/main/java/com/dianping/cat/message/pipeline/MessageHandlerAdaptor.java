package com.dianping.cat.message.pipeline;

import com.dianping.cat.message.Log;
import com.dianping.cat.message.LogSegment;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.MetricBag;

public class MessageHandlerAdaptor implements MessageHandler {
	@Override
	public int getOrder() {
		return 0;
	}

	protected void handleLog(MessageHandlerContext ctx, Log log) {
		ctx.fireMessage(log);
	}

	protected void handleLogSegment(MessageHandlerContext ctx, LogSegment logSegment) {
		ctx.fireMessage(logSegment);
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (msg instanceof MessageTree) {
			handleMessagreTree(ctx, (MessageTree) msg);
		} else if (msg instanceof Log) {
			handleLog(ctx, (Log) msg);
		} else if (msg instanceof LogSegment) {
			handleLogSegment(ctx, (LogSegment) msg);
		} else if (msg instanceof Metric) {
			handleMetric(ctx, (Metric) msg);
		} else if (msg instanceof MetricBag) {
			handleMetricBag(ctx, (MetricBag) msg);
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

	protected void handleMetricBag(MessageHandlerContext ctx, MetricBag metricBag) {
		ctx.fireMessage(metricBag);
	}
}
