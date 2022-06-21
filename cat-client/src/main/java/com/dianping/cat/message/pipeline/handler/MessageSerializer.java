package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.MetricBag;
import com.dianping.cat.message.encoder.MessageTreeEncoder;
import com.dianping.cat.message.encoder.MetricBagEncoder;
import com.dianping.cat.message.encoder.NativeMessageTreeEncoder;
import com.dianping.cat.message.encoder.NativeMetricBagEncoder;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * Bytes layout:
 * 
 * <pre>
 * | content length | content bytes |
 *                                            
 * |     4 bytes    |    {length}   |
 * </pre>
 * 
 * @author qmwu2000
 */
// Component
public class MessageSerializer extends MessageHandlerAdaptor {
	public static String ID = "message-tree-serializer";

	private MessageTreeEncoder m_messageEncoder = new NativeMessageTreeEncoder();

	private MetricBagEncoder m_metricEncoder = new NativeMetricBagEncoder();

	@Override
	public int getOrder() {
		return 300;
	}

	@Override
	protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer();
		int writerIndex = buf.writerIndex();

		buf.writeInt(0); // length placeholder
		m_messageEncoder.encode(tree, buf);

		int size = buf.readableBytes();

		buf.setInt(writerIndex, size - 4); // actual length

		ctx.fireMessage(buf); // deliver the ByteBuf to the next handler
	}

	@Override
	protected void handleMetricBag(MessageHandlerContext ctx, MetricBag bag) {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer();
		int writerIndex = buf.writerIndex();

		buf.writeInt(0); // length placeholder
		m_metricEncoder.encode(bag, buf);

		int size = buf.readableBytes();

		buf.setInt(writerIndex, size - 4); // actual length

		ctx.fireMessage(buf); // deliver the ByteBuf to the next handler
	}
}
